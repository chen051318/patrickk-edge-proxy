import com.alibaba.fastjson.JSON;
import com.tuya.edgegateway.client.domain.ndp.CmdBuilderFactory;
import com.tuya.edgegateway.client.domain.ndp.common.ICmd;
import com.tuya.edgegateway.client.domain.ndp.common.IssueDataCmd;
import com.tuya.edgegateway.client.domain.ndp.dc._234.OpenDoorCmd;
import com.tuya.edgegateway.client.domain.ndp.dc.cardinfo.CardInfoDataAdd;
import com.tuya.edgegateway.client.domain.ndp.dc.cardinfo.CardInfoDataDel;
import com.tuya.edgegateway.client.domain.ndp.dc.cardinfo.CardInfoDataEnable;
import com.tuya.edgegateway.client.domain.ndp.dc.cardinfo.CardInfoDataUpdate;
import com.tuya.edgegateway.client.domain.ndp.dc.doorbell.DoorBellCmd;
import com.tuya.edgegateway.client.domain.ndp.dc.faceinfo.FaceInfoDataAdd;
import com.tuya.edgegateway.client.domain.ndp.dc.faceinfo.FaceInfoDataDel;
import com.tuya.edgegateway.client.domain.ndp.dc.faceinfo.FaceInfoDataUpdate;
import com.tuya.edgegateway.client.domain.ndp.dc.qrcodeinfo.QrCodeInfoDataAdd;
import com.tuya.edgegateway.client.domain.ndp.dc.qrcodeinfo.QrCodeInfoDataDel;
import com.tuya.edgegateway.client.domain.ndp.dc.qrcodeinfo.QrCodeInfoDataMultiAdd;
import com.tuya.edgegateway.client.domain.ndp.dc.qrcodeinfo.QrCodeInfoDataUpdate;
import com.tuya.edgegateway.client.domain.ndp.dc.userinfo.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * DoorControlCmdBuilders Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>12月 3, 2019</pre>
 */
public class DoorControlCmdBuildersTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void userDel() {
        IssueDataCmd<UserInfoDataDel> userInfoDataIssueDataCmd = CmdBuilderFactory.doorControlCmdBuilders().issueUserInfoDataDelCmdBuilder()
                //.withBsn("bsn")
                .dataBuilder()
                .withUid("uid")
                .parentBuilder()
                .build();
        System.out.println(userInfoDataIssueDataCmd.toJsonDP());
        System.out.println(JSON.toJSONString(userInfoDataIssueDataCmd));
    }


    @Test
    public void userAdd() {
        IssueDataCmd<UserInfoDataAdd> userInfoDataIssueDataCmd = CmdBuilderFactory.doorControlCmdBuilders().issueUserInfoDataAddCmdBuilder()
                .withPreSn("123444")
                .dataBuilder()
                .withUid("uid")
                .withName("username")
                .withIdCard("idcard")
                .withPhone("17788881111")
                .withBeginTime(System.currentTimeMillis())
                .withEndTime(System.currentTimeMillis())
                .parentBuilder()
                .build();

        System.out.println(userInfoDataIssueDataCmd.toJsonDP());
        System.out.println(JSON.toJSONString(userInfoDataIssueDataCmd));
    }

    @Test
    public void userUpdate() {
        Long beginTime = null;
        IssueDataCmd<UserInfoDataUpdate> userInfoDataIssueDataCmd = CmdBuilderFactory.doorControlCmdBuilders().issueUserInfoDataUpdateCmdBuilder()
                //.withBsn("bsn")
                .dataBuilder()
                .withUid("uid")
                .withName("username")
                .withIdCard("idcard")
                .withPhone("17788881111")
                .withBeginTime(beginTime)
                .withEndTime(System.currentTimeMillis())
                .parentBuilder()
                .build();
        System.out.println(userInfoDataIssueDataCmd.toJsonDP());
        System.out.println(JSON.toJSONString(userInfoDataIssueDataCmd));
    }

    @Test
    public void userEnable() {
        IssueDataCmd<UserInfoStatusManage> build = CmdBuilderFactory.doorControlCmdBuilders().issueUserInfoStatusManageCmdBuilder()
                //.withBsn("bsn")
                .dataBuilder()
                .withUid("uid")
                .withEnabled(1)
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));

    }

    @Test
    public void faceAdd() {
        IssueDataCmd<FaceInfoDataAdd> build = CmdBuilderFactory.doorControlCmdBuilders().issueFaceInfoDataAddCmdBuilder()
                .dataBuilder()
                .withFaceId("faceid")
                .withUid("uid")
                .withUrl("url")
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void faceUpdate() {
        IssueDataCmd<FaceInfoDataUpdate> build = CmdBuilderFactory.doorControlCmdBuilders().issueFaceInfoDataUpdateCmdBuilder()
                .dataBuilder()
                .withFaceId("faceid")
                .withUid("uid")
                .withUrl("url")
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void faceDel() {
        IssueDataCmd<FaceInfoDataDel> build = CmdBuilderFactory.doorControlCmdBuilders().issueFaceInfoDataDelCmdBuilder()
                .dataBuilder()
                .withFaceId("faceid")
                .withUid("uid")
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));

    }

    @Test
    public void cardAdd() {
        IssueDataCmd<CardInfoDataAdd> build = CmdBuilderFactory.doorControlCmdBuilders().issueCardDataInfoAddCmdBuilder()
                .dataBuilder()
                .withCardNo("cardNo")
                .withUid("uid")
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void cardUpdate() {
        IssueDataCmd<CardInfoDataUpdate> build = CmdBuilderFactory.doorControlCmdBuilders().issueCardDataInfoUpdateCmdBuilder()
                .dataBuilder()
                .withCardNo("cardNo")
                .withOldCardNo("oldCardNo")
                .withUid("uid")
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void cardDel() {
        IssueDataCmd<CardInfoDataDel> build = CmdBuilderFactory.doorControlCmdBuilders().issueCardDataInfoDelCmdBuilder()
                .dataBuilder()
                .withUid("uid")
                .withCardNo("cardNo")
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void qrCodeEnable() {
        IssueDataCmd<CardInfoDataEnable> build = CmdBuilderFactory.doorControlCmdBuilders().issueCardDataInfoEnableCmdBuilder()
                .dataBuilder()
                .withCardNo("cardNo")
                .withUid("uid")
                .withEnable(1)
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }



    @Test
    public void qrCodeAdd() {
        IssueDataCmd<QrCodeInfoDataAdd> build = CmdBuilderFactory.doorControlCmdBuilders().issueQrCodeDataInfoAddCmdBuilder()
                .dataBuilder()
                .withQrCode("qrCode")
                .withUid("uid")
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void qrCodeMultiAdd() {

        List<QrCodeInfoDataAdd> qrCodeInfoDataAddList = new ArrayList<>();
        QrCodeInfoDataAdd qrCodeInfoDataAdd = new QrCodeInfoDataAdd();
        qrCodeInfoDataAdd.setQrcode("qrCode");
        qrCodeInfoDataAdd.setUid("uid");

        QrCodeInfoDataAdd qrCodeInfoDataAdd1 = new QrCodeInfoDataAdd();
        qrCodeInfoDataAdd1.setQrcode("qrCode1");
        qrCodeInfoDataAdd1.setUid("uid1");

        qrCodeInfoDataAddList.add(qrCodeInfoDataAdd);
        qrCodeInfoDataAddList.add(qrCodeInfoDataAdd1);


        IssueDataCmd<QrCodeInfoDataMultiAdd> build = CmdBuilderFactory.doorControlCmdBuilders().issueQrCodeDataInfoMultiAddCmdBuilder()
                .dataBuilder()
                .withQrCodeInfoList(qrCodeInfoDataAddList)
                .parentBuilder()
                .build();

        System.out.println(JSON.toJSONString(build));

    }

    @Test
    public void qrCodeUpdate() {
        IssueDataCmd<QrCodeInfoDataUpdate> build = CmdBuilderFactory.doorControlCmdBuilders().issueQrCodeDataInfoUpdateCmdBuilder()
                .dataBuilder()
                .withQrCode("qrcode")
                .withUid("uid")
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void qrCodeDel() {
        IssueDataCmd<QrCodeInfoDataDel> build = CmdBuilderFactory.doorControlCmdBuilders().issueQrCodeDataInfoDelCmdBuilder()
                .dataBuilder()
                .withUid("uid")
                .parentBuilder()
                .build();
        System.out.println(JSON.toJSONString(build));
    }

    /**
     * 开门指令的builder对象
     *
     * @return
     */
    @Test
    public void openDoorCmdBuilder() {
        OpenDoorCmd openDoorCmd = CmdBuilderFactory.doorControlCmdBuilders().openDoorCmdBuilder()
                .withName("test")
                .withUid("uid")
                .build();
        openDoorCmd.setSn(123L);
        System.out.println(openDoorCmd.toJsonDP());
        System.out.println(JSON.toJSONString(openDoorCmd));

    }

    @Test
    public void doorBellCmdBuilder() {
        DoorBellCmd build = CmdBuilderFactory.doorControlCmdBuilders().doorBellCmdBuilder()
                .withType(1)
                .withUid("uid")
                .withTargetAddress("1001")
                .withDeviceId("deviceId")
                .build();

        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void clearUser() {
        IssueDataCmd<UserInfoDataClear> build = CmdBuilderFactory.doorControlCmdBuilders().issueUserInfoDataClearCmdBuilder()
                .dataBuilder()
                .parentBuilder()
                .build();
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void testIssueEncrypt() {
        IssueDataCmd<UserInfoDataClear> build = CmdBuilderFactory.doorControlCmdBuilders().issueUserInfoDataClearCmdBuilder()
                .dataBuilder()
                .parentBuilder()
                .build();


        System.out.println(build.toJsonDP());
    }

    @Test
    public void aa() {
        ICmd cmd = CmdBuilderFactory.lightControlCmdBuilders().lightBrightCmdBuilder()
                .withBright(1)
                .build();
        ICmd cmd1 = CmdBuilderFactory.lightControlCmdBuilders().groupLightBrightCmdBuilder()
                .withGroupNo(23)
                .withBright(1)
                .build();
        String s1 = cmd1.toJsonDP();
        String s = cmd.toJsonDP();
    }
}
