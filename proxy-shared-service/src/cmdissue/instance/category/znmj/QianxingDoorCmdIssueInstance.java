package cmdissue.instance.category.znmj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tuya.dev.center.assist.client.face.domain.request.FaceFeatureRequest;
import com.tuya.dev.center.assist.client.face.domain.vo.FaceFeatureVO;
import com.tuya.edgegateway.client.domain.ndp.CmdBuilderFactory;
import com.tuya.edgegateway.client.domain.ndp.common.ICmd;
import com.tuya.edgegateway.client.domain.ndp.dc.faceinfo.FaceInfoDataAdd;
import com.tuya.edgegateway.integration.service.assist.IFaceFeatureServiceClient;
import com.tuya.edgegateway.manager.cmdissue.domain.CmdIssueRecordDTO;
import cmdissue.instance.CmdIssueStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;


/**
 * 前行门禁指令发送
 */
@CmdIssueStrategy(productType = "wf_znmj", vendorCode = "qianxing")
@Component("wf_znmj_qianxing")
@Slf4j
public class QianxingDoorCmdIssueInstance extends DoorCmdIssueInstance {
    @Resource
    private IFaceFeatureServiceClient faceFeatureServiceClient;

    @Override
    public void issue(CmdIssueRecordDTO cmdIssueDTO) {
        boolean flag = doFilter(cmdIssueDTO);
        if (flag) {
            return;
        }

        if (!StringUtils.equals("128", cmdIssueDTO.getDpid())) {
            super.issueNotFilter(cmdIssueDTO);
            return;
        }

        String dpData = cmdIssueDTO.getData();

        JSONObject jsonObj = JSON.parseObject(dpData);
        JSONObject dpObj = jsonObj.getJSONObject("128");

        String mode = dpObj.getString("mode");
        String type = dpObj.getString("type");

        //人脸新增指令
        if (StringUtils.equals("dc_faceInfo", mode) && (StringUtils.equals("add", type) || StringUtils.equals("update", type))) {
            rebuildFaceCmd(cmdIssueDTO, type);
        }
        super.issueNotFilter(cmdIssueDTO);
    }


    /**
     * 重新构建人脸相关指令
     *
     * @param cmdIssueDTO
     * @param type
     */
    private void rebuildFaceCmd(CmdIssueRecordDTO cmdIssueDTO, String type) {
        String dpData = cmdIssueDTO.getData();
        JSONObject jsonObj = JSON.parseObject(dpData);
        JSONObject dpObj = jsonObj.getJSONObject("128");
        FaceInfoDataAdd faceInfoDataAdd = dpObj.getObject("data", FaceInfoDataAdd.class);

        String uid = faceInfoDataAdd.getUid();
        String faceId = faceInfoDataAdd.getFaceId();
        String url = faceInfoDataAdd.getUrl();

        String featureCont = null;
        //如果业务方已经转换成特征，则不再处理
        if (StringUtils.isNotBlank(faceInfoDataAdd.getFeatureCont())) {
            featureCont = faceInfoDataAdd.getFeatureCont();
        } else {
            FaceFeatureRequest request = new FaceFeatureRequest();
            request.setUid(uid);
            request.setImageId(faceId);
            request.setPicUrl(url);
            FaceFeatureVO qianxingFaceFeatureVO = faceFeatureServiceClient.generateQianxingFaceFeature(request);
            if (qianxingFaceFeatureVO != null) {
                featureCont = qianxingFaceFeatureVO.getFeatureCont();
            } else {
                log.warn("QianxingDoorCmdIssueInstance feature error, deviceId: {},dpObj:{}", cmdIssueDTO.getDeviceId(), dpObj);
            }
        }
        byte[] nameBytes = null;
        //如果业务方已经处理，则不再处理
        if (faceInfoDataAdd.getNameBytes() != null) {
            nameBytes = faceInfoDataAdd.getNameBytes();
        } else {
            if (StringUtils.isNotEmpty(faceInfoDataAdd.getName())) {
                try {
                    nameBytes = faceInfoDataAdd.getName().getBytes("GB2312");
                } catch (UnsupportedEncodingException e) {
                    log.warn("QianxingDoorCmdIssueInstance issue fail ,rebuildFaceCmd nameBytes fail name: {}", faceInfoDataAdd.getName());
                }
            }
        }
        ICmd cmd;
        if (StringUtils.equals("add", type)) {
            cmd = CmdBuilderFactory.doorControlCmdBuilders().issueFaceInfoDataAddCmdBuilder()
                    .dataBuilder()
                    .withUid(uid)
                    .withFaceId(faceId)
//                    .withUrl(url)
                    .withFeatureCont(featureCont)
                    .withNameBytes(nameBytes)
                    .parentBuilder()
                    .build();
        } else {
            cmd = CmdBuilderFactory.doorControlCmdBuilders().issueFaceInfoDataUpdateCmdBuilder()
                    .dataBuilder()
                    .withUid(uid)
                    .withFaceId(faceId)
//                    .withUrl(url)
                    .withFeatureCont(featureCont)
                    .withNameBytes(nameBytes)
                    .parentBuilder()
                    .build();
        }
        cmd.setSn(cmdIssueDTO.getSn());
        cmdIssueDTO.setData(cmd.toJsonDP());
    }

}
