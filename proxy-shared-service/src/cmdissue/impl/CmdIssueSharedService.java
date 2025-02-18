package cmdissue.impl;

import com.alibaba.fastjson.JSON;
import com.tuya.edgegateway.client.common.PageResult;
import com.tuya.edgegateway.client.common.Paging;
import com.tuya.edgegateway.client.domain.cmd.IssueCmdResultVO;
import com.tuya.edgegateway.client.domain.cmd.constants.CmdPriorityEnum;
import com.tuya.edgegateway.client.domain.cmd.constants.ProcessStatusEnum;
import com.tuya.edgegateway.client.domain.ndp.common.ICmd;
import com.tuya.edgegateway.common.utils.ProxyStringUtils;
import com.tuya.edgegateway.core.cmdissue.domain.CmdIssueRecordQuery;
import com.tuya.edgegateway.core.cmdissue.domain.CmdIssueUpdateCondition;
import com.tuya.edgegateway.core.util.apollo.ApolloConfigUtil;
import com.tuya.edgegateway.core.util.sensitivelog.LogMarkers;
import com.tuya.edgegateway.integration.service.athena.IDeviceMServiceClient;
import com.tuya.edgegateway.integration.service.caesar.IDpPublishServiceClient;
import com.tuya.edgegateway.integration.service.caesar.IMessagePushServiceClient;
import com.tuya.edgegateway.manager.base.configuration.TraceAgent;
import com.tuya.edgegateway.manager.base.exception.EdgeException;
import com.tuya.edgegateway.manager.base.utils.BizIdGenerator;
import com.tuya.edgegateway.manager.base.utils.PagingUtils;
import com.tuya.edgegateway.manager.base.utils.RetryUtils;
import com.tuya.edgegateway.manager.cmdissue.ICmdIssueManager;
import com.tuya.edgegateway.manager.cmdissue.domain.CmdIssueRecordDTO;
import com.tuya.edgegateway.manager.cmdissue.domain.IssueDataCmdResultDTO;
import com.tuya.edgegateway.manager.device.IDeviceManager;
import com.tuya.edgegateway.manager.device.IGatewayManager;
import com.tuya.edgegateway.manager.device.domain.DeviceDTO;
import com.tuya.edgegateway.manager.device.domain.GatewayDTO;
import com.tuya.edgegateway.manager.kafka.domain.KafkaCmdBatchIssueMessage;
import com.tuya.edgegateway.manager.kafka.domain.KafkaCmdIssueMessage;
import com.tuya.edgegateway.manager.kafka.producer.IKafkaMsgProducer;
import cmdissue.ICmdGroupIssueWay;
import cmdissue.ICmdIssueDelaySharedService;
import cmdissue.ICmdIssueSharedService;
import cmdissue.model.convert.CmdIssueRecordConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author: patrickkk
 * date： 2019/12/9
 */
@Service
@Slf4j
public class CmdIssueSharedService implements ICmdIssueSharedService {

    /**
     * 时间间隔因子
     */
    @Value("${cmd.interval.factor:30}")
    private int intervalFactor;

    @Resource
    private ICmdIssueDelaySharedService cmdIssueDelaySharedService;

    @Resource
    private IDeviceManager deviceManager;

    @Resource
    private IGatewayManager gatewayManager;

    @Resource
    private ICmdIssueManager cmdIssueManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private TraceAgent traceAgent;

    @Resource
    private BizIdGenerator bizIdGenerator;

    @Resource
    private IKafkaMsgProducer kafkaMsgProducer;

    @Resource
    private IDpPublishServiceClient dpPublishServiceClient;

    @Resource
    private IMessagePushServiceClient messagePushServiceClient;

    @Resource
    private IDeviceMServiceClient deviceMServiceClient;

    @Override
    public IssueCmdResultVO issueDeviceDpCommand(String uidForIOT, String deviceId, ICmd cmd, CmdPriorityEnum priorityEnum) {
        cmd.setSn(bizIdGenerator.generateId());
        log.info(LogMarkers.SENSITIVE, "issueDeviceDpCommand input, uid: {}, deviceId: {}, cmd: {}, priority: {}",
                uidForIOT, deviceId, cmd.toJsonDP(), priorityEnum.getCode());
        return issueSingleDeviceCommand(uidForIOT, deviceId, cmd, priorityEnum,
                (cmd1) -> dpPublishServiceClient.issueDpCommand(uidForIOT, deviceId, cmd.getCmdVersion(), cmd.getSn(), cmd.toJsonDP()));
    }

    @Override
    public IssueCmdResultVO issueDeviceProtocolCommand(String uidForIOT, String deviceId, ICmd cmd) {
        cmd.setSn(bizIdGenerator.generateId());
        log.info(LogMarkers.SENSITIVE, "issueDeviceProtocolCommand input, uid: {}, deviceId: {}, cmd: {}", uidForIOT, deviceId, cmd.toJsonDP());
        return issueSingleDeviceCommand(cmd.getUid(), deviceId, cmd, CmdPriorityEnum.HIGH,
                (cmd1) -> messagePushServiceClient.issueMobileProtocolCommand(cmd.getReqType(), cmd.getUserBizType(), cmd.getUid(), JSON.toJSONString(cmd)));
    }

    @Override
    public void compensate(final CmdIssueRecordDTO cmdIssueDTO) {
        CmdIssueUpdateCondition updateCondition = new CmdIssueUpdateCondition();
        updateCondition.setSn(cmdIssueDTO.getSn());
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    try {
                        log.info("compensate, 开始指令补偿,sn: {}", cmdIssueDTO.getSn());

                        //【1】查询并锁住指令记录
                        CmdIssueRecordDTO cmdIssueDTO0 = cmdIssueManager.queryCmdIssueBySn(cmdIssueDTO.getSn(), true);

                        if (cmdIssueDTO0 == null) {
                            log.error("compensate, 待补偿的指令记录为空,sn = {}", cmdIssueDTO.getSn());
                            return;
                        }

                        //执行成功或挂起
                        if (cmdIssueDTO0.getProcessStatus() == 4 || cmdIssueDTO0.getProcessStatus() == 5) {
                            log.warn(LogMarkers.SENSITIVE, "compensate, 待补偿的指令记录状态为执行成功或挂起,不需要补偿. cmdIssueRecordDTO = {}", cmdIssueDTO0);
                            return;
                        }

                        //【2】指令发送次数校验
                        boolean flag = overMaxRetryCount(cmdIssueDTO0);
                        if (flag) {
                            return;
                        }

                        //【3】指令重新发送
                        KafkaCmdBatchIssueMessage kafkaCmdBatchIssueMessage = new KafkaCmdBatchIssueMessage();
                        kafkaCmdBatchIssueMessage.setSn(cmdIssueDTO0.getSn());
                        kafkaCmdBatchIssueMessage.setSource("compensate");
                        kafkaCmdBatchIssueMessage.setDeviceId(cmdIssueDTO0.getDeviceId());
                        kafkaMsgProducer.pushCmdBatchIssueMessage(kafkaCmdBatchIssueMessage);

                        //【4】更新指令状态
                        //计算指令下一次执行的时间
                        cmdIssueDTO0.setRetryCount(cmdIssueDTO0.getRetryCount() + 1);
                        flag = overMaxRetryCount(cmdIssueDTO0);
                        if (flag) {
                            return;
                        }

                        updateCondition.setRetryCount(cmdIssueDTO0.getRetryCount());
                        long nextProcessTime = RetryUtils.calcNextProcessTime(cmdIssueDTO0.getNextProcessTime(), updateCondition.getRetryCount(), intervalFactor);
                        cmdIssueDTO0.setNextProcessTime(nextProcessTime);
                        updateCondition.setNextProcessTime(nextProcessTime);
                        updateCondition.setMemo("已重试");
                        cmdIssueManager.modifyProcessStatusBySn(updateCondition);

                        cmdIssueDelaySharedService.sendCmdResultMessage(cmdIssueDTO0, cmdIssueDTO0.getProcessStatus(), cmdIssueDTO0.getMemo());
                        return;
                    } catch (Exception ex) {
                        log.error("compensate, 指令补偿异常, sn:" + cmdIssueDTO.getSn(), ex);
                        transactionStatus.setRollbackOnly();
                        throw new EdgeException("下发指令补偿异常", ex);
                    }
                }
            });
        } catch (Exception ex) {
            log.error("compensate, 指令补偿异常,直接更新重试次数,sn: {}", cmdIssueDTO.getSn());
            updateCondition.setRetryCount(cmdIssueDTO.getRetryCount() + 1);
            updateCondition.setMemo("指令补偿事务回滚,直接更新重试次数");
            cmdIssueManager.modifyProcessStatusBySn(updateCondition);
        }
    }

    @Override
    public void dealCmdIssueResult(IssueDataCmdResultDTO issueCmdResultDTO) {
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    try {
                        //【1】 查询指令记录并校验
                        CmdIssueRecordDTO cmdIssueDTO0 = cmdIssueManager.queryCmdIssueBySn(issueCmdResultDTO.getSn(), true);
                        if (cmdIssueDTO0 == null) {
                            log.error("指令不存在, sn: {}", issueCmdResultDTO.getSn());
                            return;
                        }

                        //指令已经执行成功,不处理
                        if (cmdIssueDTO0.getProcessStatus() == ProcessStatusEnum.EXEC_SUCC.getValue()) {
                            return;
                        }

                        // 设置返回数据
                        if (!StringUtils.isEmpty(issueCmdResultDTO.getData())) {
                            log.info(LogMarkers.SENSITIVE, "[CmdIssueSharedService.dealCmdIssueResult] sn: {}, data: {}", issueCmdResultDTO.getSn(), issueCmdResultDTO.getData());
                            cmdIssueDTO0.setCmdBizType(issueCmdResultDTO.getCmdBizType());
                            cmdIssueDTO0.setBizId(issueCmdResultDTO.getBizId());
                            cmdIssueDTO0.setResponseData(issueCmdResultDTO.getData());
                        }

                        //【2】更新指令记录
                        String projectId = cmdIssueDTO0.getProjectId();
                        CmdIssueUpdateCondition updateCondition = new CmdIssueUpdateCondition();
                        if (issueCmdResultDTO.getSuccess() == 1) {
                            traceAgent.logCmdSuccessOrFailRatio().record(1, 0);
                            traceAgent.logCmdExecuteBackSuccess(projectId);
                            updateCondition.setProcessStatus(ProcessStatusEnum.EXEC_SUCC.getValue());
                        } else {
                            log.warn("dealCmdIssueResult fail, sn: {}, devId: {}, msg: {}",
                                    cmdIssueDTO0.getSn(), cmdIssueDTO0.getDeviceId(), issueCmdResultDTO.getMessage());
                            traceAgent.logExecuteError();
                            traceAgent.logCmdExecuteBackFail(projectId);

                            if (cmdIssueDTO0.getProcessStatus() != ProcessStatusEnum.PENDING.getValue()) {
                                updateCondition.setProcessStatus(ProcessStatusEnum.EXEC_FAIL.getValue());
                            } else {
                                updateCondition.setProcessStatus(ProcessStatusEnum.PENDING.getValue());
                            }
                        }

                        updateCondition.setSn(issueCmdResultDTO.getSn());
                        updateCondition.setMsg(ProxyStringUtils.subStringByByte(issueCmdResultDTO.getMessage(), 100, StandardCharsets.UTF_8));
                        cmdIssueManager.modifyProcessStatusBySn(updateCondition);

                        //【3】发送kafka消息
                        cmdIssueDelaySharedService.sendCmdResultMessage(cmdIssueDTO0, updateCondition.getProcessStatus(), issueCmdResultDTO.getMessage());
                    } catch (Exception ex) {
                        log.error("dealCmdIssueResult, 事务回滚, sn: " + issueCmdResultDTO.getSn(), ex);
                        transactionStatus.setRollbackOnly();
                        throw new EdgeException("下发指令补偿异常", ex);
                    }
                }
            });
        } catch (Exception ex) {
            log.error("dealCmdIssueResult error, sn: " + issueCmdResultDTO.getSn(), ex);
        }
    }


    @Override
    public PageResult<CmdIssueRecordDTO> queryListByCondition(CmdIssueRecordQuery condition, Paging paging) {
        condition.setOffset(paging.getStart());
        condition.setLimit(paging.getLimit());
        // 按照时间和sn排序
        condition.setOrderBy("gmt_modified desc, sn desc");

        long count = cmdIssueManager.count(condition);
        if (count < 0) {
            return PagingUtils.toEmptyPageResult(paging);
        }
        List<CmdIssueRecordDTO> list = cmdIssueManager.list(condition);
        return PagingUtils.toPageResult(list, count, paging);
    }

    private IssueCmdResultVO issueSingleDeviceCommand(String uidForIOT, String deviceId, ICmd cmd, CmdPriorityEnum
            priorityEnum, ICmdGroupIssueWay cmdIssueWay) {
        CmdIssueRecordDTO cmdIssueRecordDTO = null;
        DeviceDTO deviceDTO = null;
        try {
            if (cmd.getGateway()) {//网关指令
                GatewayDTO gatewayDTO = gatewayManager.queryByGatewayId(deviceId);
                deviceDTO = convertGatewayDTO(gatewayDTO);
            } else { //设备指令
                deviceDTO = deviceManager.queryDeviceByDeviceId(deviceId);
            }

            if (deviceDTO == null) {
                log.error("issueSingleDeviceCommand, 下发指令查询设备记录为空, deviceId: {}", deviceId);
                throw new EdgeException("下发指令查询设备记录为空!");
            }

            cmdIssueRecordDTO = CmdIssueRecordConvert.convert2CmdIssueRecord(uidForIOT, deviceDTO, cmd, intervalFactor);

            if (StringUtils.isNotBlank(cmd.getPreSn())) {//有依赖sn
                cmdIssueRecordDTO.setProcessStatus(ProcessStatusEnum.SAVED.getValue());
                cmdIssueManager.save(cmdIssueRecordDTO);
            } else {
                switch (priorityEnum) {
                    case LOW:
                        cmdIssueRecordDTO.setProcessStatus(ProcessStatusEnum.SAVED.getValue());
                        cmdIssueManager.save(cmdIssueRecordDTO);

                        KafkaCmdBatchIssueMessage kafkaCmdBatchIssueMsg = new KafkaCmdBatchIssueMessage();
                        kafkaCmdBatchIssueMsg.setSn(cmd.getSn());
                        kafkaCmdBatchIssueMsg.setDeviceId(cmdIssueRecordDTO.getDeviceId());
                        kafkaCmdBatchIssueMsg.setSource("issueSingleDeviceCommand");
                        kafkaMsgProducer.pushCmdBatchIssueMessage(kafkaCmdBatchIssueMsg);
                        break;
                    case MIDDLE:
                        cmdIssueRecordDTO.setProcessStatus(ProcessStatusEnum.SAVED.getValue());
                        cmdIssueManager.save(cmdIssueRecordDTO);

                        KafkaCmdIssueMessage kafkaCmdIssueMsg = new KafkaCmdIssueMessage();
                        kafkaCmdIssueMsg.setSn(cmd.getSn());
                        kafkaCmdIssueMsg.setDeviceId(cmdIssueRecordDTO.getDeviceId());
                        kafkaCmdIssueMsg.setSource("issueSingleDeviceCommand");
                        kafkaMsgProducer.pushCmdIssueMessage(kafkaCmdIssueMsg);
                        break;
                    case HIGH:
                        if (StringUtils.equals(cmdIssueRecordDTO.getDpid(), "0")) { // 协议指令
                            // 校验是否离线
                            boolean online = deviceMServiceClient.isOnline(deviceId);
                            if (!online) {
                                log.error("issueSingleDeviceCommand, 该设备已离线, deviceId: {}", deviceId);
                                cmdIssueRecordDTO.setMsg("设备已经离线");
                                cmdIssueRecordDTO.setProcessStatus(ProcessStatusEnum.SENDED_FAIL.getValue());
                                cmdIssueManager.save(cmdIssueRecordDTO);
                                break;
                            }
                            try {
                                cmdIssueWay.dpPublish(cmd);
                                cmdIssueRecordDTO.setProcessStatus(ProcessStatusEnum.SENDED_SUCC.getValue());
                            } catch (Exception ex) {
                                cmdIssueRecordDTO.setMsg("指令发送时异常");
                                cmdIssueRecordDTO.setProcessStatus(ProcessStatusEnum.SENDED_FAIL.getValue());
                                log.error("issueSingleDeviceCommand, 指令发送时异常, deviceId: " + deviceId, ex);
                            }
                            cmdIssueManager.save(cmdIssueRecordDTO);
                        } else {//非协议指令
                            cmdIssueRecordDTO.setProcessStatus(ProcessStatusEnum.SAVED.getValue());
                            cmdIssueManager.save(cmdIssueRecordDTO);
                            cmdIssueDelaySharedService.improveIssueCmd(cmdIssueRecordDTO.getSn());
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            cmdIssueRecordDTO = CmdIssueRecordConvert.convert2CmdIssueRecord(uidForIOT, deviceDTO, cmd, intervalFactor);
            cmdIssueRecordDTO.setMsg(ProxyStringUtils.subStringByByte(ex.getMessage(), 100, StandardCharsets.UTF_8));
            log.error("issueSingleDeviceCommand error, 指令下发失败, deviceId: " + deviceId, ex);
        }

        //返回指令发送结果
        IssueCmdResultVO issueCmdResultVO = CmdIssueRecordConvert.
                convert2CmdResultVO(cmdIssueRecordDTO, priorityEnum == CmdPriorityEnum.HIGH ? 2 : 6);
        return issueCmdResultVO;
    }

    private boolean overMaxRetryCount(CmdIssueRecordDTO cmdIssueDTO) {
        int maxRetryCount = ApolloConfigUtil.getCmdMaxRetryCount();
        if (cmdIssueDTO.getRetryCount() >= maxRetryCount) {
            //指令重试次数已经超过最大次数,直接挂起
            CmdIssueUpdateCondition updateCondition = new CmdIssueUpdateCondition();
            updateCondition.setSn(cmdIssueDTO.getSn());
            updateCondition.setRetryCount(cmdIssueDTO.getRetryCount());
            updateCondition.setProcessStatus(ProcessStatusEnum.PENDING.getValue());
            updateCondition.setMsg("设备侧未返回, 指令执行结果未知");
            cmdIssueManager.modifyProcessStatusBySn(updateCondition);

            //发送消息
            cmdIssueDelaySharedService.sendCmdResultMessage(cmdIssueDTO, 5, updateCondition.getMsg());
            return true;
        }
        return false;
    }

    /**
     * 网关对象DTO转为设备对象DTO
     *
     * @param gatewayDTO
     * @return
     */
    private DeviceDTO convertGatewayDTO(GatewayDTO gatewayDTO) {
        if (gatewayDTO == null) {
            return null;
        }

        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setDeviceId(gatewayDTO.getGatewayId());
        deviceDTO.setProductType(gatewayDTO.getProductType());
        deviceDTO.setProductId(gatewayDTO.getProductId());
        deviceDTO.setProjectId(gatewayDTO.getProjectId());
        deviceDTO.setGatewayId(gatewayDTO.getGatewayId());
        return deviceDTO;
    }
}
