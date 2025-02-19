package impl.stategy.strategy.impl.devicelog;

import com.alibaba.fastjson.JSON;
import com.x.atop.client.domain.api.ApiRequestDO;
import com.x.edgegateway.atop.device.impl.strategy.annotation.EdgeStrategy;
import com.x.edgegateway.atop.device.impl.strategy.impl.AbstractReportProcessor;
import com.x.edgegateway.common.model.DeviceStrategy;
import com.x.edgegateway.manager.base.utils.BeanPropertyCopyUtils;
import com.x.edgegateway.shared.service.log.DeviceLogUploadDTO;
import com.x.edgegateway.shared.service.log.ILogRecordSharedService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @author patrickkk
 * @date 2020/5/15
 */
@EdgeStrategy(tag = DeviceStrategy.Log.UPLOAD_TAG)
@Service
public class DeviceLogResultReport extends AbstractReportProcessor {

    @Resource
    private ILogRecordSharedService logRecordSharedService;

    @Override
    public void report(ApiRequestDO apiRequestDO, String data) {
        DeviceLogUploadRequest uploadRequest = JSON.parseObject(data, DeviceLogUploadRequest.class);
        Assert.isTrue(uploadRequest.getSn() > 0, "sn不能为空！");

        onReceivedCmdResult(uploadRequest.getSn(), uploadRequest.getSuccess(), uploadRequest.getMessage(), null, null);

        // 添加日志记录
        DeviceLogUploadDTO deviceLogUploadDTO = new DeviceLogUploadDTO();
        BeanPropertyCopyUtils.copy(uploadRequest, deviceLogUploadDTO);
        logRecordSharedService.modifyLogRecord(deviceLogUploadDTO);
    }
}
