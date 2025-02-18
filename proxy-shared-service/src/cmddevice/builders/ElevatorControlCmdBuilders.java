package cmddevice.builders;

import com.tuya.edgegateway.client.domain.ndp.common.IssueDataCmdBuilder;
import com.tuya.edgegateway.client.domain.ndp.ec.cmd.ElevatorControlCmdBuilder;
import com.tuya.edgegateway.client.domain.ndp.ec.cmd.FloorAuthGroupCmdBuilder;
import com.tuya.edgegateway.client.domain.ndp.ec.cmd.cardinfo.*;
import com.tuya.edgegateway.client.domain.ndp.ec.cmd.faceinfo.*;
import com.tuya.edgegateway.client.domain.ndp.ec.cmd.qrcodeinfo.*;
import com.tuya.edgegateway.client.domain.ndp.ec.cmd.userinfo.*;

/**
 *
 * @author patrickkk  2020/8/13 14:59
 */
public final class ElevatorControlCmdBuilders {

    private static ElevatorControlCmdBuilders elevatorControlCmdBuilders = new ElevatorControlCmdBuilders();

    private ElevatorControlCmdBuilders() {
    }

    public static ElevatorControlCmdBuilders getInstance() {
        return elevatorControlCmdBuilders;
    }

    /**
     * 获取梯控指令builder
     *
     * @return
     */
    public ElevatorControlCmdBuilder elevatorControlCmdBuilder() {
            return ElevatorControlCmdBuilder.anElevatorControlCmd();
    }

    /**
     * 获取梯控楼层权限builder
     *
     * @return
     */
    public FloorAuthGroupCmdBuilder elevatorFloorAuthGroupCmdBuilder() {
        return FloorAuthGroupCmdBuilder.anFloorAuthGroupCmd();
    }


    /**
     * 构造用户添加请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorUserInfoAdd, ElevatorUserInfoAddBuilder> issueElevatorUserInfoAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorUserInfoAddBuilder.anUserInfoData());
    }


    /**
     * 构造用户更新请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorUserInfoUpdate, ElevatorUserInfoUpdateBuilder> issueElevatorUserInfoUpdateCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorUserInfoUpdateBuilder.anUserInfoData());
    }

    /**
     * 构造用户删除请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorUserInfoDel, ElevatorUserInfoDelBuilder> issueElevatorUserInfoDelCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorUserInfoDelBuilder.anUserInfoData());
    }


    /**
     * 构造人脸添加请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorFaceInfoAdd, ElevatorFaceInfoAddBuilder> issueElevatorFaceInfoAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorFaceInfoAddBuilder.anFaceInfoData());
    }

    /**
     * 构造人脸更新请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorFaceInfoUpdate, ElevatorFaceInfoUpdateBuilder> issueElevatorFaceInfoUpdateCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorFaceInfoUpdateBuilder.anFaceInfoData());
    }

    /**
     * 构造人脸更删除求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorFaceInfoDel, ElevatorFaceInfoDelBuilder> issueElevatorFaceInfoDelCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorFaceInfoDelBuilder.anFaceInfoData());
    }


    /**
     * 构造二维码添加实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorQrCodeInfoAdd, ElevatorQrCodeInfoAddBuilder> issueElevatorQrCodeInfoAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorQrCodeInfoAddBuilder.anQrCodeInfoData());
    }

    /**
     * 构造二维码更新实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorQrCodeInfoUpdate, ElevatorQrCodeInfoUpdateBuilder> issueElevatorQrCodeInfoUpdateCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorQrCodeInfoUpdateBuilder.anQrCodeInfoData());
    }

    /**
     * 构造二维码删除实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorQrCodeInfoDel, ElevatorQrCodeInfoDelBuilder> issueElevatorQrCodeInfoDelCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorQrCodeInfoDelBuilder.anQrCodeInfoData());
    }

    /**
     * 构造梯控卡创建实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorCardInfoDataAdd, ElevatorCardInfoDataAddBuilder> issueElevatorCardInfoAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorCardInfoDataAddBuilder.anCardInfoData());
    }
    /**
     * 构造梯控卡删除实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorCardInfoDataDel, ElevatorCardInfoDataDelBuilder> issueElevatorCardInfoDelCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorCardInfoDataDelBuilder.anCardInfoData());
    }

    /**
     * 构造梯控卡冻结实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorCardInfoDataDisable, ElevatorCardInfoDataDisableBuilder> issueElevatorCardInfoFreezeCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorCardInfoDataDisableBuilder.anCardInfoData());
    }

    /**
     * 构造梯控卡解冻实例
     *
     * @return
     */
    public IssueDataCmdBuilder<ElevatorCardInfoDataEnable, ElevatorCardInfoDataEnableBuilder> issueElevatorCardInfoUnfreezeCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(ElevatorCardInfoDataEnableBuilder.anCardInfoData());
    }


}
