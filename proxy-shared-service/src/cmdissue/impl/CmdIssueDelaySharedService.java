package cmdissue.impl;

import cmdissue.ICmdIssueDelaySharedService;
import cmdissue.instance.CmdIssueFactory;
import cmdissue.instance.ICmdIssueInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author patrickkk  2021/7/21 14:53
 */
@Service
@Slf4j
public class CmdIssueDelaySharedService implements ICmdIssueDelaySharedService {

    @Resource
    private IKafkaMsgProducer kafkaMsgProducer;

    @Resource
    private IKafkaPulsarProducer kafkaPulsarProducer;

    @Resource
    private IDeviceSharedService deviceSharedService;

    @Resource
    private ICmdIssueManager cmdIssueManager;

    @Resource
    private ProductCfgManager productCfgManager;

    @Resource
    private IDeviceMServiceClient deviceMServiceClient;

    @Resource
    private IGrpcConsumerServiceClient grpcConsumerServiceClient;

    @Resource
    private ThreadPoolTaskExecutor deviceOfflineCheckThreadExecutor;

    @Resource
    private CmdIssueFactory cmdIssueFactory;

    @Resource
    private EdgeLockHelper edgeLockHelper;

    /**
     * uuid分布式锁key
     */
    private static final String LOCK_KEY = "edge_delay_cmd_lock_";

    /**
     * 缓存设备下一次执行指令时间
     */
    private static final String DELAY_CMD_KEY = "edge_delay_cmd_lock_";

    /**
     * 获取uuid分布式锁阻塞时间(毫秒)
     */
    private static final Long LOCK_WAIT_TIME = 3000L;

    /**
     * 同一个设备下一次执行指令的时间key(毫秒)
     */
    private static final Long LOCK_RELEASE_TIME = 5000L;

    @Resource
    private LokiClient<String, Long> lokiClient;

    @Override
    public void improveIssueCmd(Long sn) {
        //【1】校验是否已经发送成功
        CmdIssueRecordDTO cmdIssueDTO = cmdIssueManager.queryCmdIssueBySn(sn, false);
        if (cmdIssueDTO == null) {
            log.error("improveIssueCmd, cmd record is null, don't handle, sn: {}", sn);
            return;
        }

        if (cmdIssueDTO.getProcessStatus() == ProcessStatusEnum.EXEC_SUCC.getValue() ||
                cmdIssueDTO.getProcessStatus() == ProcessStatusEnum.EXEC_FAIL.getValue()
                || cmdIssueDTO.getProcessStatus() == ProcessStatusEnum.PENDING.getValue()) {
            log.warn("improveIssueCmd, cmd already handle succ or fail, don't handle, sn: {}", sn);
            return;
        }

        //【2】控制类指令,直接发送
        if (cmdIssueDTO.getDataFlag() == 2) {
            issueGeneralCmd(cmdIssueDTO);
            //加入到延迟队列, 设备未返回结果的时候, 更新指令的状态为未知
            Long cmdIntervalFactor = ApolloConfigUtil.getCmdIntervalFactor();

            //加入到延迟队列
            CmdIssueDelayDTO cmdIssueDelayDTO = new CmdIssueDelayDTO(cmdIssueDTO.getSn(), cmdIssueDTO.getDeviceId());
            grpcConsumerServiceClient.sendDelayTask(cmdIssueDTO.getDeviceId(), cmdIntervalFactor,
                    DelayMsgTypeEnum.CMD_ISSUE.getCode(), cmdIssueDelayDTO);
            return;
        }

        //【3】非控制类指令
        ProductCfgDTO productCfgDTO = productCfgManager.load(cmdIssueDTO.getProductId(), true);

        //【3.1】没有配置指令延迟发送时间间隔, 走原有逻辑
        if (productCfgDTO.getCmdInterval() == null || productCfgDTO.getCmdInterval() <= 0) {
            issueGeneralCmd(cmdIssueDTO);
            return;
        }

        //【3.2】 配置指令延迟发送时间间隔, 走延迟队列
        Long nextProcessTime = calcNextProcessTime(cmdIssueDTO.getDeviceId(), Long.valueOf(productCfgDTO.getCmdInterval()));
        CmdIssueUpdateCondition updateCondition = new CmdIssueUpdateCondition();
        updateCondition.setSn(cmdIssueDTO.getSn());
        updateCondition.setRetryCount(-1);
        updateCondition.setNextProcessTime(nextProcessTime);
        updateCondition.setMemo("指令首次加入到延迟队列");
        cmdIssueManager.modifyProcessStatusBySn(updateCondition);

        //加入到延迟队列
        CmdIssueDelayDTO cmdIssueDelayDTO = new CmdIssueDelayDTO(cmdIssueDTO.getSn(), cmdIssueDTO.getDeviceId());
        grpcConsumerServiceClient.sendSimpleTask(cmdIssueDTO.getDeviceId(), nextProcessTime,
                DelayMsgTypeEnum.CMD_ISSUE.getCode(), cmdIssueDelayDTO);
    }

    /**
     * 通用指令发送
     *
     * @param cmdIssueDTO
     */
    @Override
    public void issueGeneralCmd(CmdIssueRecordDTO cmdIssueDTO) {
        String productType = cmdIssueDTO.getProductType();
        String vendorCode = cmdIssueDTO.getSupplierCode();

        //【1】 校验设备是否在线
        boolean online = deviceMServiceClient.isOnline(cmdIssueDTO.getDeviceId());
        if (!online) {
            Integer offlineCheck = ApolloConfigUtil.getDriveOfflineCheck();
            if (offlineCheck.equals(1)) {
                deviceOfflineCheckThreadExecutor.execute(() -> {
                    try {
                        deviceSharedService.deviceOfflineCheck(cmdIssueDTO);
                    } catch (Exception e) {
                        log.error("deviceOfflineCheckPool error", e);
                    }
                });
            }

            //todo aipad门禁走特殊逻辑
            if (StringUtils.equals("wf_znmj", productType) && StringUtils.equals("x", vendorCode)
                    && StringUtils.equals("128", cmdIssueDTO.getDpid())) {
                //数据同步到酒店, 门禁主动拉取指令
                ICmdIssueInstance cmdIssueInstance = cmdIssueFactory.getInstance(productType, vendorCode);
                cmdIssueInstance.issue(cmdIssueDTO);
            } else {
                log.warn("issueGeneralCmd, device offline, don't handle, deviceId:{}, sn:{}", cmdIssueDTO.getDeviceId(), cmdIssueDTO.getSn());
            }
            return;
        }

        //【2】指令发送
        String dpData = cmdIssueDTO.getData();
        log.info(LogMarkers.SENSITIVE, "issueGeneralCmd, deviceId: {}, sn: {}, send data:{}", cmdIssueDTO.getDeviceId(), cmdIssueDTO.getSn(), dpData);

        ICmdIssueInstance cmdIssueInstance = cmdIssueFactory.getInstance(productType, vendorCode);
        cmdIssueInstance.issue(cmdIssueDTO);
    }

    @Override
    public Long calcNextProcessTime(String deviceId, Long cmdIntervalFactor) {
        String lockKey = LOCK_KEY + deviceId;
        String retryKey = DELAY_CMD_KEY + deviceId;
        long nextRetryTime = 0;
        try {
            if (!edgeLockHelper.lock(lockKey, LOCK_WAIT_TIME, LOCK_RELEASE_TIME)) {
                nextRetryTime = System.currentTimeMillis() + cmdIntervalFactor * 1000;
                log.warn("calcNextProcessTime, gain lock fail");
                return nextRetryTime;
            }

            Long nowTime = System.currentTimeMillis();
            Long lastRetryTime = lokiClient.opsForValue().get(retryKey);
            if (lastRetryTime == null || lastRetryTime <= nowTime) {
                nextRetryTime = nowTime + cmdIntervalFactor * 1000;
            } else {
                nextRetryTime = lastRetryTime + cmdIntervalFactor * 1000;
            }

            //缓存有效期10分钟
            long timeout = nextRetryTime + 10 * 60 * 1000;
            lokiClient.opsForValue().set(retryKey, nextRetryTime, timeout, TimeUnit.MILLISECONDS);
        } catch (Throwable ex) {
            log.warn("calcNextProcessTime, gain next process time fail!");
            //redies不可用时
            nextRetryTime = System.currentTimeMillis() + cmdIntervalFactor * 1000;
        } finally {
            log.info("calcNextProcessTime, realease lock!");
            edgeLockHelper.unLock(lockKey);
        }
        return nextRetryTime;
    }

    /**
     * 发送指令执行消息
     *
     * @param cmdIssueDTO
     * @param processStatus
     */
    @Override
    public void sendCmdResultMessage(CmdIssueRecordDTO cmdIssueDTO, int processStatus, String message) {
        try {
            long happenTime = System.currentTimeMillis();
            //【1】发送kafka消息
            KafkaCmdResultMessage cmdResultMessage = new KafkaCmdResultMessage();
            cmdResultMessage.setSn(cmdIssueDTO.getSn());
            cmdResultMessage.setBsn(cmdIssueDTO.getBsn());
            cmdResultMessage.setDeviceId(cmdIssueDTO.getDeviceId());
            cmdResultMessage.setRetryCount(cmdIssueDTO.getRetryCount());
            cmdResultMessage.setNextProcessTime(cmdIssueDTO.getNextProcessTime());
            cmdResultMessage.setSendTime(happenTime);
            cmdResultMessage.setProcessStatus(processStatus);
            cmdResultMessage.setSuccess(processStatus == 4 ? 1 : 0);
            cmdResultMessage.setMessage(message);
            cmdResultMessage.setDataFlag(cmdIssueDTO.getDataFlag());

            //业务属性
            cmdResultMessage.setCmdBizType(cmdIssueDTO.getCmdBizType());
            cmdResultMessage.setBizId(cmdIssueDTO.getBizId());
            cmdResultMessage.setData(cmdIssueDTO.getResponseData());

            kafkaMsgProducer.pushCmdResultMessage(cmdResultMessage);

            //【2】发送pulsar消息
            KafkaPulsarCmdResultMessage pulsarCmdResultMessage = new KafkaPulsarCmdResultMessage();

            PulsarCmdResultDTO pulsarCmdResultDTO = new PulsarCmdResultDTO();
            pulsarCmdResultDTO.setSn(cmdIssueDTO.getSn());
            pulsarCmdResultDTO.setProcessStatus(convertCmd2PulsarStatus(processStatus));
            pulsarCmdResultDTO.setMessage(message);
            pulsarCmdResultDTO.setHappenTime(happenTime);
            //业务属性
            pulsarCmdResultDTO.setCmdBizType(cmdIssueDTO.getCmdBizType());
            pulsarCmdResultDTO.setBizId(cmdIssueDTO.getBizId());
            pulsarCmdResultDTO.setData(cmdIssueDTO.getResponseData());

            pulsarCmdResultMessage.setDevId(cmdIssueDTO.getDeviceId());
            pulsarCmdResultMessage.setProductId(cmdIssueDTO.getProductId());
            pulsarCmdResultMessage.setData(pulsarCmdResultDTO);
            kafkaPulsarProducer.pushCmdResultMessage(pulsarCmdResultMessage);
        } catch (Exception ex) {
            log.error("sendCmdResultMessage error.", ex);
        }
    }

    /**
     * 指令执行状态转换
     *
     * @param processStatus
     * @return
     */
    private Integer convertCmd2PulsarStatus(int processStatus) {
        if (ProcessStatusEnum.SENDED_SUCC.getValue() == processStatus) {
            //指令已发送
            return 2;
        }else if (ProcessStatusEnum.EXEC_SUCC.getValue() == processStatus) {
            //指令执行成功
            return 3;
        } else if (ProcessStatusEnum.EXEC_FAIL.getValue() == processStatus) {
            //指令执行失败
            return 4;
        } else if (ProcessStatusEnum.PENDING.getValue() == processStatus) {
            //指令执行状态未知
            return 5;
        } else {
            //指令发送中
            return 1;
        }
    }
}
