package cmdissue.model.convert;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author ：公明
 * @Date ：Created in 2020/11/17 4:14 下午
 * @Description：
 */
public class EdgeFacePicConvert {

    public static List<LabelVO> convert2LabelVO(List<EdgeFacePicLabelDTO> edgeFacePicLabelDTOs) {

        List<LabelVO> labelVOS = Lists.newArrayList();

        if (CollectionUtils.isEmpty(edgeFacePicLabelDTOs)) {
            return labelVOS;
        }

        for (EdgeFacePicLabelDTO each : edgeFacePicLabelDTOs) {
            LabelVO labelVO = new LabelVO();

            labelVO.setSex(each.getSex());
            labelVO.setSexConfidence(each.getSexConfidence());
            labelVO.setAge(each.getAge());
            labelVO.setAgeConfidence(each.getAgeConfidence());
            labelVO.setGlass(each.getGlass());
            labelVO.setHasGlassConfidence(each.getGlassConfidence());
            labelVO.setBizCode(each.getBizCode());
            labelVO.setBizId(each.getBizId());
            labelVO.setSeqId(each.getSeqId());

            labelVOS.add(labelVO);
        }

        return labelVOS;

    }

    public static EdgeFacePicLabelDTO parseFaceLabel(FaceAttributeVO faceAttribute, EdgeFacePicLabelDTO label) {

        if (faceAttribute == null) {
            return label;
        }


        JSONObject genderJson = faceAttribute.getGender();

        //解析性别
        if (null != genderJson) {
            String value = genderJson.getString("value");

            //性别：男
            if (StringUtils.equals("male", value)) {
                label.setSex(1);

            }
            if (StringUtils.equals("female", value)) {
                label.setSex(0);
            }
            label.setSexConfidence(genderJson.getFloat("score"));

        }

        JSONObject ageJson = faceAttribute.getAge();

        //解析年龄
        if (null != ageJson) {
            Integer value = ageJson.getInteger("value");

            //年龄
            label.setAge(value);
            label.setAgeConfidence(ageJson.getFloat("score"));

        }

        JSONObject glassJson = faceAttribute.getGlass();

        //解析是否戴眼镜
        if (null != glassJson) {
            String value = glassJson.getString("value");

            //戴眼镜
            if (StringUtils.equals("none", value)) {
                label.setGlass(0);
            }
            if (StringUtils.equals("common", value) || StringUtils.equals("sun", value)) {
                label.setGlass(1);
            }
            label.setGlassConfidence(glassJson.getFloat("score"));

        }

        return label;
    }


}
