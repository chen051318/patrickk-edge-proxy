package cmddevice.builders;

import com.tuya.edgegateway.client.domain.ndp.common.IssueDataCmdBuilder;
import com.tuya.edgegateway.client.domain.ndp.dc._234.OpenDoorCmdBuilder;
import com.tuya.edgegateway.client.domain.ndp.dc.cardinfo.*;
import com.tuya.edgegateway.client.domain.ndp.dc.doorbell.AppCallDeviceCmdBuilder;
import com.tuya.edgegateway.client.domain.ndp.dc.doorbell.DoorBellCmdBuilder;
import com.tuya.edgegateway.client.domain.ndp.dc.faceinfo.*;
import com.tuya.edgegateway.client.domain.ndp.dc.maintain.DeviceMaintainCmdBuilder;
import com.tuya.edgegateway.client.domain.ndp.dc.passpwd.*;
import com.tuya.edgegateway.client.domain.ndp.dc.qrcodeinfo.*;
import com.tuya.edgegateway.client.domain.ndp.dc.userinfo.*;

/**
 * @author: patrickkk
 * date： 2019/12/3
 */
public final class DoorControlCmdBuilders {

    private static final DoorControlCmdBuilders doorControlCmdBuilders = new DoorControlCmdBuilders();

    private DoorControlCmdBuilders() {
    }

    /**
     * 构造用户添加请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<UserInfoDataAdd, UserInfoDataAddBuilder> issueUserInfoDataAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(UserInfoDataAddBuilder.anUserInfoData());
    }


    /**
     * 构造用户更新请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<UserInfoDataUpdate, UserInfoDataUpdateBuilder> issueUserInfoDataUpdateCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(UserInfoDataUpdateBuilder.anUserInfoData());
    }

    /**
     * 构造用户删除请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<UserInfoDataDel, UserInfoDataDelBuilder> issueUserInfoDataDelCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(UserInfoDataDelBuilder.anUserInfoData());
    }

    /**
     * 构造用户通行权限指令
     *
     * @return
     */
    public IssueDataCmdBuilder<UserInfoStatusManage, UserInfoStatusManageBuilder> issueUserInfoStatusManageCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(UserInfoStatusManageBuilder.anUserInfoData());
    }

    /**
     * 构造用户清空指令
     *
     * @return
     */
    public IssueDataCmdBuilder<UserInfoDataClear, UserInfoDataClearBuilder> issueUserInfoDataClearCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(UserInfoDataClearBuilder.anUserInfoData());
    }

    /**
     * 构造人脸添加请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<FaceInfoDataAdd, FaceInfoDataAddBuilder> issueFaceInfoDataAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(FaceInfoDataAddBuilder.anFaceInfoData());
    }

    /**
     * 构造人脸更新请求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<FaceInfoDataUpdate, FaceInfoDataUpdateBuilder> issueFaceInfoDataUpdateCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(FaceInfoDataUpdateBuilder.anFaceInfoData());
    }

    /**
     * 构造人脸更删除求实例
     *
     * @return
     */
    public IssueDataCmdBuilder<FaceInfoDataDel, FaceInfoDataDelBuilder> issueFaceInfoDataDelCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(FaceInfoDataDelBuilder.anFaceInfoData());
    }

    /**
     * 构造门卡添加实例
     *
     * @return
     */
    public IssueDataCmdBuilder<CardInfoDataAdd, CardInfoDataAddBuilder> issueCardDataInfoAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(CardInfoDataAddBuilder.anCardInfoData());
    }

    /**
     * 构造门卡修改实例
     *
     * @return
     */
    public IssueDataCmdBuilder<CardInfoDataUpdate, CardInfoDataUpdateBuilder> issueCardDataInfoUpdateCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(CardInfoDataUpdateBuilder.anCardInfoData());
    }

    /**
     * 构造门卡删除实例
     *
     * @return
     */
    public IssueDataCmdBuilder<CardInfoDataDel, CardInfoDataDelBuilder> issueCardDataInfoDelCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(CardInfoDataDelBuilder.anCardInfoData());
    }

    /**
     * 构造门卡冻结解冻实例
     *
     * @return
     */
    public IssueDataCmdBuilder<CardInfoDataEnable, CardInfoDataEnableBuilder> issueCardDataInfoEnableCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(CardInfoDataEnableBuilder.anCardInfoData());
    }

    /**
     * 构造二维码添加实例
     *
     * @return
     */
    public IssueDataCmdBuilder<QrCodeInfoDataAdd, QrCodeInfoDataAddBuilder> issueQrCodeDataInfoAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(QrCodeInfoDataAddBuilder.anQrCodeInfoData());
    }

    /**
     * 构造批量新增用户请求
     *
     * @return
     */
    public IssueDataCmdBuilder<QrCodeInfoDataMultiAdd, QrCodeInfoDataMultiAddBuilder> issueQrCodeDataInfoMultiAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(QrCodeInfoDataMultiAddBuilder.anQrCodeInfoData());
    }


    /**
     * 构造二维码更新实例
     *
     * @return
     */
    public IssueDataCmdBuilder<QrCodeInfoDataUpdate, QrCodeInfoDataUpdateBuilder> issueQrCodeDataInfoUpdateCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(QrCodeInfoDataUpdateBuilder.anQrCodeInfoData());
    }

    /**
     * 构造二维码删除实例
     *
     * @return
     */
    public IssueDataCmdBuilder<QrCodeInfoDataDel, QrCodeInfoDataDelBuilder> issueQrCodeDataInfoDelCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(QrCodeInfoDataDelBuilder.anQrCodeInfoData());
    }



    public static DoorControlCmdBuilders getInstance() {
        return doorControlCmdBuilders;
    }

    /**
     * 开门指令的builder对象
     *
     * @return
     */
    public OpenDoorCmdBuilder openDoorCmdBuilder() {
        return OpenDoorCmdBuilder.anOpenDoorCmd();
    }

    /**
     * 设备呼叫开门的指令
     *
     * @return
     */
    public DoorBellCmdBuilder doorBellCmdBuilder() {
        return DoorBellCmdBuilder.anDoorBellCmd();
    }

    /**
     * app呼叫设备指令
     *
     * @return
     */
    public AppCallDeviceCmdBuilder deviceCallAppCmdBuilder() {
        return AppCallDeviceCmdBuilder.anAppCallDeviceCmdBuilder();
    }

    /**
     * 设备呼叫开门的指令
     *
     * @return
     */
    public DeviceMaintainCmdBuilder deviceMaintainCmdBuilder() {
        return DeviceMaintainCmdBuilder.anDeviceActivateCmdData();
    }


    /**
     * 构造门禁通行密码添加实例
     *
     * @return
     */
    public IssueDataCmdBuilder<PassPwdDataAdd, PassPwdDataAddBuilder> issuePassPwdDataInfoAddCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(CardInfoDataAddBuilder.anCardInfoData());
    }

    /**
     * 构造门禁通行密码修改实例
     *
     * @return
     */
    public IssueDataCmdBuilder<PassPwdDataUpdate, PassPwdDataUpdateBuilder> issuePassPwdDataInfoUpdateCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(CardInfoDataUpdateBuilder.anCardInfoData());
    }

    /**
     * 构造门禁通行密码删除实例
     *
     * @return
     */
    public IssueDataCmdBuilder<PassPwdDataDel, PassPwdDataDelBuilder> issuePassPwdDataInfoDelCmdBuilder() {
        return IssueDataCmdBuilder.asIssueDataCmdBuilder(CardInfoDataDelBuilder.anCardInfoData());
    }

}
