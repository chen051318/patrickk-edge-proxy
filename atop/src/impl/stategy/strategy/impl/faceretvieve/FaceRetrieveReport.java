package impl.stategy.strategy.impl.faceretvieve;

import com.alibaba.fastjson.JSON;
import com.tuya.atop.client.domain.api.ApiRequestDO;
import com.tuya.edgegateway.atop.device.impl.strategy.annotation.EdgeStrategy;
import com.tuya.edgegateway.atop.device.impl.strategy.impl.AbstractReportProcessor;
import com.tuya.edgegateway.client.domain.cmd.constants.CmdBizTypeEnum;
import com.tuya.edgegateway.manager.ai.domain.EdgeFaceRetrieveDTO;
import com.tuya.edgegateway.manager.base.exception.EdgeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : patrickkk
 * @date 2021-09-27
 */
@EdgeStrategy(tag = "faceRetrieve" + "response")
@Service
@Slf4j
public class FaceRetrieveReport extends AbstractReportProcessor {

    @Override
    public void report(ApiRequestDO apiRequestDO, String data) {
        if (StringUtils.isEmpty(data)) {
            throw new EdgeException("Invalid data");
        }

        FaceRetrieveRequest req = JSON.parseObject(data, FaceRetrieveRequest.class);
        String resultStr = null;
        if (!CollectionUtils.isEmpty(req.getFaceRetrieveItems())) {
            List<EdgeFaceRetrieveDTO> resultList = req.getFaceRetrieveItems().stream().map(s -> {
                EdgeFaceRetrieveDTO dto = new EdgeFaceRetrieveDTO();
                BeanUtils.copyProperties(s, dto);
                return dto;
            }).collect(Collectors.toList());
            resultStr = JSON.toJSONString(resultList);
        }
        onReceivedCmdResult(req.getSn(), req.getSuccess(), req.getMessage(), resultStr, CmdBizTypeEnum.EDGE_FACE_RETRIEVE_RESULT);
    }
}
