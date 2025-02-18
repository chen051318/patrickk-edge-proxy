package cmddevice.bean.ota;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhaoyu
 * @date 2020/6/8
 */
@Getter
@Setter
public class DeviceOtaData {

    /**文件类型,jar或zip*/
    private String fileType;

    /**文件名称*/
    private String fileName;

    /**下载url*/
    private String downloadUrl;

}
