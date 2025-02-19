package cmdissue.instance.category.znmj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cmdissue.instance.CmdIssueStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 狄耐克门禁指令发送
 *
 * @author patrickkk  2020/12/18 15:58
 */
@CmdIssueStrategy(productType = "wf_znmj", vendorCode = "dnake")
@Component("wf_znmj_dnake")
@Slf4j
public class DnakeDoorCmdIssueInstance extends DoorCmdIssueInstance {
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

        if (StringUtils.equals("dc_faceInfo", mode) && (StringUtils.equals("add", type) || StringUtils.equals("update", type))) {
            //新增和更新字段一样,用新增的Data即可
            FaceInfoDataAdd faceInfoDataAdd = dpObj.getObject("data", FaceInfoDataAdd.class);
            //如果业务方已经转换成特征，则不再处理
            if(StringUtils.isNotBlank(faceInfoDataAdd.getFeatureUrl())||StringUtils.isNotBlank(faceInfoDataAdd.getFeatureMd5())){
                super.issueNotFilter(cmdIssueDTO);
                return;
            }
            String uid = faceInfoDataAdd.getUid();
            String faceId = faceInfoDataAdd.getFaceId();
            String url = faceInfoDataAdd.getUrl();
            FaceFeatureRequest request = new FaceFeatureRequest();
            request.setUid(uid);
            request.setImageId(faceId);
            request.setPicUrl(url);
            FaceFeatureVO faceFeatureVO = faceFeatureServiceClient.generateFaceFeature(request);
            if (faceFeatureVO != null) {
                ICmd cmd;
                if (StringUtils.equals("add", type)) {
                    cmd = CmdBuilderFactory.doorControlCmdBuilders().issueFaceInfoDataAddCmdBuilder()
                            .dataBuilder()
                            .withUid(uid)
                            .withFaceId(faceId)
                            .withUrl(url)
                            .withFeatureUrl(faceFeatureVO.getFeatureUrl())
                            .withFeatureMd5(faceFeatureVO.getFeatureMd5())
                            .parentBuilder()
                            .build();
                } else {
                    cmd = CmdBuilderFactory.doorControlCmdBuilders().issueFaceInfoDataUpdateCmdBuilder()
                            .dataBuilder()
                            .withUid(uid)
                            .withFaceId(faceId)
                            .withUrl(url)
                            .withFeatureUrl(faceFeatureVO.getFeatureUrl())
                            .withFeatureMd5(faceFeatureVO.getFeatureMd5())
                            .parentBuilder()
                            .build();
                }

                cmd.setSn(cmdIssueDTO.getSn());
                dpData = cmd.toJsonDP();

                cmdIssueDTO.setData(dpData);
            } else {
                log.warn("CmdIssueHandler DnakeDoorCmdIssueInstance feature error, deviceId: {}, send data: {}", cmdIssueDTO.getDeviceId(), dpData);
            }
        }
        super.issueNotFilter(cmdIssueDTO);
    }
}
