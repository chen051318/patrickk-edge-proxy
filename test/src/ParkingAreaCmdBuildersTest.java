import com.alibaba.fastjson.JSON;
import com.tuya.edgegateway.client.domain.ndp.CmdBuilderFactory;
import com.tuya.edgegateway.client.domain.ndp.common.ICmd;
import com.tuya.edgegateway.client.domain.ndp.common.IssueDataCmd;
import com.tuya.edgegateway.client.domain.ndp.pa.ParkingCoupon.ParkingCouponDataAdd;
import com.tuya.edgegateway.client.domain.ndp.pa.ParkingGroup.ParkingGroupDataAdd;
import com.tuya.edgegateway.client.domain.ndp.pa.ParkingGroup.ParkingGroupDataDel;
import com.tuya.edgegateway.client.domain.ndp.pa.ParkingGroup.ParkingGroupDataUpdate;
import com.tuya.edgegateway.client.domain.ndp.pa.ResidentCarPermission.Car;
import com.tuya.edgegateway.client.domain.ndp.pa.ResidentCarPermission.ResidentCarPermissionDataAdd;
import com.tuya.edgegateway.client.domain.ndp.pa.ResidentCarPermission.ResidentCarPermissionDataDel;
import com.tuya.edgegateway.client.domain.ndp.pa.ResidentCarPermission.ResidentCarPermissionDataUpdate;
import com.tuya.edgegateway.client.domain.ndp.pa.VisitorCarPermission.VisitorCarPermissionDataAdd;
import com.tuya.edgegateway.client.domain.ndp.pa.VisitorCarPermission.VisitorCarPermissionDataDel;
import com.tuya.edgegateway.client.domain.ndp.pa.VisitorCarPermission.VisitorCarPermissionDataUpdate;
import com.tuya.edgegateway.client.domain.ndp.pa.parkdevice.ParkingDeviceCmd;
import com.tuya.edgegateway.client.domain.ndp.pa.parkingOrder.ParkingOrderDataAdd;
import com.tuya.edgegateway.client.domain.ndp.pa.parkinggate.ParkingGateControlCmd;
import com.tuya.edgegateway.client.domain.ndp.pa.parkinglanes.ParkingLanesCmd;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * ParkingAreaCmdBuilders Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>12月 4, 2019</pre>
 */
public class ParkingAreaCmdBuildersTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }


    @Test
    public void testIssueResidentCarPermissionAddCmdBuilder() throws Exception {
        IssueDataCmd<ResidentCarPermissionDataAdd> build = CmdBuilderFactory.parkingAreaCmdBuilders().issueResidentCarPermissionAddCmdBuilder()
                .dataBuilder()
                .withBeginTime(System.currentTimeMillis())
                .withEndTime(System.currentTimeMillis())
                .withId("001")
                .withParkingGroupNo("tuyaceshi")
                .withCar(new Car("浙A23456", "李四", new String[] {"1", "2", "3"}, "12312312312313123123"))
                .withCar(new Car("浙A12345", "张三", new String[] {"1", "2", "3"}, "12312312312313123123"))
                .withCar(new Car("浙A79190", "王二", new String[] {"1", "2", "3"}, "12312312312313123123"))
                .parentBuilder()
                .build();
        Assert.assertEquals(build.getDpid(), new Integer(1));
        System.out.println(build.toJsonDP());
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void testIssueResidentCarPermissionUpdateCmdBuilder() throws Exception {
        IssueDataCmd<ResidentCarPermissionDataUpdate> build = CmdBuilderFactory.parkingAreaCmdBuilders().issueResidentCarPermissionUpdateCmdBuilder()
                .dataBuilder()
                .withBeginTime(System.currentTimeMillis())
                .withEndTime(System.currentTimeMillis())
                .withId("002")
                .withParkingGroupNo("tuyaceshi")
                .withCar(new Car("浙A23456", "tuya", new String[] {"1", "2", "3"}))
                .withCar(new Car("浙A34567", "张三", new String[] {"1", "2", "3"}))
                .parentBuilder()
                .build();

        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void testIssueResidentCarPermissionDelCmdBuilder() throws Exception {
        IssueDataCmd<ResidentCarPermissionDataDel> build = CmdBuilderFactory.parkingAreaCmdBuilders().issueResidentCarPermissionDelCmdBuilder()
                .dataBuilder()
                .withId("id")
                .parentBuilder()
                .build();
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void testIssueVisitorCarPermissionCmdAddBuilder() {
        IssueDataCmd<VisitorCarPermissionDataAdd> issueDataCmd = CmdBuilderFactory.parkingAreaCmdBuilders().issueVisitorCarPermissionAddCmdBuilder()
                .dataBuilder()
                .withPlateNo("浙A01785")
                .withId("003")
                .withBeginTime(System.currentTimeMillis())
                .withEndTime(System.currentTimeMillis())
                .withUserId("1234")
                .withVisitorId("2345")
                .withVisitorName("涂鸦")
                .parentBuilder()
                .build();
        Assert.assertEquals(issueDataCmd.getDpid(), new Integer(1));
        System.out.println(JSON.toJSONString(issueDataCmd));
    }

    @Test
    public void testIssueVisitorCarPermissionCmdUpdateBuilder() {
        IssueDataCmd<VisitorCarPermissionDataUpdate> issueDataCmd = CmdBuilderFactory.parkingAreaCmdBuilders().issueVisitorCarPermissionUpdateCmdBuilder()
                .dataBuilder()
                .withId("003")
                .withPlateNo("浙A01785")
                .withBeginTime(System.currentTimeMillis())
                .withEndTime(System.currentTimeMillis())
                .withUserId("1234")
                .withVisitorId("4567")
                .withVisitorName("tuya1")
                .parentBuilder()
                .build();
        Assert.assertEquals(issueDataCmd.getDpid(), new Integer(1));
        System.out.println(JSON.toJSONString(issueDataCmd));
    }

    @Test
    public void testIssueVisitorCarPermissionCmdDelBuilder() {
        IssueDataCmd<VisitorCarPermissionDataDel> issueDataCmd = CmdBuilderFactory.parkingAreaCmdBuilders().issueVisitorCarPermissionDelCmdBuilder()
                .withBsn("")
                .dataBuilder()
                .withPlateNo("浙A01785")
                .withId("003")
                .parentBuilder()
                .build();
        Assert.assertEquals(issueDataCmd.getDpid(), new Integer(1));
        System.out.println(JSON.toJSONString(issueDataCmd));
    }

    @Test
    public void testIssueParkingGroupDataAddBuilder() {
        List<String> parkings = Arrays.asList("1", "2");
        IssueDataCmd<ParkingGroupDataAdd> build = CmdBuilderFactory.parkingAreaCmdBuilders().issueParkingGroupDataAddCmdBuilder()
                .dataBuilder()
                .withFullType(1)
                .withId("001")
                .withParkingList(parkings)
                .withParkingGroupNo("1")
                .parentBuilder()
                .build();
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void testIssueParkingGroupDataUpdateBuilder() {
        List<String> parkings = Arrays.asList("2", "3");
        IssueDataCmd<ParkingGroupDataUpdate> build = CmdBuilderFactory.parkingAreaCmdBuilders().issueParkingGroupDataUpdateCmdBuilder()
                .dataBuilder()
                .withFullType(1)
                .withId("001")
                .withParkingList(parkings)
                .withParkingGroupNo("1")
                .parentBuilder()
                .build();
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void testIssueParkingGroupDataDelBuilder() {
        IssueDataCmd<ParkingGroupDataDel> build = CmdBuilderFactory.parkingAreaCmdBuilders().issueParkingGroupDataDelCmdBuilder()
                .dataBuilder()
                .withId("001")
                .withParkingGroupNo("1")
                .parentBuilder()
                .build();
        System.out.println(JSON.toJSONString(build));
    }


    @Test
    public void testIssueParkingCouponBuilder(){
        IssueDataCmd<ParkingCouponDataAdd> build = CmdBuilderFactory.parkingAreaCmdBuilders().issueParkingCouponAddBuilder()
                .dataBuilder()
                .withParkingPlateNo("浙A12345")
                .withParkingCouponType(1)
                .withParkingCouponValue(999999999999999999L)
                .parentBuilder()
                .build();
        System.out.println(JSON.toJSONString(build));
    }

    @Test
    public void testIssueParkingOrderBuilder(){
        IssueDataCmd<ParkingOrderDataAdd> build = CmdBuilderFactory.parkingAreaCmdBuilders().issueParkingOrderAddBuilder()
                .dataBuilder()
                .withParkingPlateNo("浙A12345")
                .withParkingOrderNo("39755e5ca2e4eb5d")
                .parentBuilder()
                .build();
        System.out.println(JSON.toJSONString(build));
    }


    @Test
    public void getParkingLanes() {
        ParkingLanesCmd parkingLanesCmd = CmdBuilderFactory.parkingAreaCmdBuilders().parkingLanesCmdBuilder()
                .withBsn("bsn")
                .build();

        System.out.println(parkingLanesCmd.toJsonDP());
        System.out.println(JSON.toJSONString(parkingLanesCmd));

    }


    @Test
    public void getParkingDevice() {
        ParkingDeviceCmd parkingDeviceCmd = CmdBuilderFactory.parkingAreaCmdBuilders().parkingDeviceCmdBuilder()
                .withBsn("bsn")
                .build();

        System.out.println(parkingDeviceCmd.toJsonDP());
        System.out.println(JSON.toJSONString(parkingDeviceCmd));
    }

    @Test
    public void test() {
        String[] strArr = new String[3];
        strArr[0] = "11";
        strArr[1] = "22";

        String s = JSON.toJSONString(strArr);

        System.out.println(s);
    }

    /**
     * 车闸指令的builder对象
     */
    @Test
    public void parkingGateControlCmdBuilder() {
        ParkingGateControlCmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().parkingGateControlCmdBuilder()
                .withDeviceId("devicedId")
                .withOpenModel(0)
                .build();

        System.out.println(cmd.toJsonDP());
        System.out.println(JSON.toJSONString(cmd));
    }

    @Test
    public void visitorCarPermissionDelayBuilder() {
        ICmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().visitorCarPermissionDelayCmdBuilder()
                .dataBuilder()
                .withId("1")
                .withBeginTime(1)
                .withEndTime(2)
                .parentBuilder()
                .build();

        System.out.println(JSON.toJSONString(cmd));
        System.out.println(cmd.toJsonDP());
        String jsonDp = "{1:\"{\\\"data\\\":{\\\"beginTime\\\":1,\\\"endTime\\\":2,\\\"id\\\":\\\"1\\\"},\\\"gateway\\\":true,\\\"mode\\\":\\\"visitorCarPermissionDelay\\\",\\\"type\\\":\\\"update\\\"}\"}";
        Assert.assertEquals(cmd.toJsonDP(), jsonDp);
    }

    @Test
    public void specialCarAddBuilder() {
        ICmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().specialCarAddCmdBuilder()
                .dataBuilder()
                .withType("test_type")
                .withPlateNo("aaa")
                .parentBuilder()
                .build();

        System.out.println(JSON.toJSONString(cmd));
        System.out.println(cmd.toJsonDP());
        String jsonDp = "{1:\"{\\\"data\\\":{\\\"plateNo\\\":\\\"aaa\\\",\\\"type\\\":\\\"test_type\\\"},\\\"gateway\\\":true,\\\"mode\\\":\\\"specialCar\\\",\\\"type\\\":\\\"add\\\"}\"}";
        Assert.assertEquals(cmd.toJsonDP(), jsonDp);
    }

    @Test
    public void specialCarDelBuilder() {
        ICmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().specialCarDelCmdBuilder()
                .dataBuilder()
                .withType("test_type")
                .withPlateNo("aaa")
                .parentBuilder()
                .build();

        System.out.println(JSON.toJSONString(cmd));
        System.out.println(cmd.toJsonDP());
        String jsonDp = "{1:\"{\\\"data\\\":{\\\"plateNo\\\":\\\"aaa\\\",\\\"type\\\":\\\"test_type\\\"},\\\"gateway\\\":true,\\\"mode\\\":\\\"specialCar\\\",\\\"type\\\":\\\"del\\\"}\"}";
        Assert.assertEquals(cmd.toJsonDP(), jsonDp);
    }

    @Test
    public void unlicensedCarPassBuilder() {
        ICmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().unlicensedCarPassCmdBuilder()
                .withDeviceId("dev")
                .withIdentifyCode("aaa")
                .build();

        System.out.println(JSON.toJSONString(cmd));
        System.out.println(cmd.toJsonDP());
        String jsonDp = "{1:\"{\\\"identifyCode\\\":\\\"aaa\\\",\\\"deviceId\\\":\\\"dev\\\",\\\"gateway\\\":true,\\\"mode\\\":\\\"unlicensedCarPass\\\"}\"}";
        Assert.assertEquals(cmd.toJsonDP(), jsonDp);
    }

    @Test
    public void payFeeBuilder() {
        ICmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().payFeeCmdBuilder()
                .withPaidAmount(100)
                .withOrderNo("aaa")
                .withPayMethod(1)
                .withPayTime(100)
                .withPayerName("dd")
                .withPayerMobile("13722232")
                .build();

        System.out.println(JSON.toJSONString(cmd));
        System.out.println(cmd.toJsonDP());
        String jsonDp = "{1:\"{\\\"gateway\\\":true,\\\"mode\\\":\\\"payFee\\\",\\\"orderNo\\\":\\\"aaa\\\",\\\"paidAmount\\\":100,\\\"payMethod\\\":1,\\\"payTime\\\":100,\\\"payerMobile\\\":\\\"13722232\\\",\\\"payerName\\\":\\\"dd\\\"}\"}";
        Assert.assertEquals(cmd.toJsonDP(), jsonDp);
    }

    @Test
    public void paymentGenerateBuilder() {
        ICmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().paymentGenerateCmdBuilder()
                .withFeeType(2)
                .withPlateNo("aaa")
                .build();

        System.out.println(JSON.toJSONString(cmd));
        System.out.println(cmd.toJsonDP());
        String jsonDp = "{1:\"{\\\"feeType\\\":2,\\\"gateway\\\":true,\\\"mode\\\":\\\"paymentGenerate\\\",\\\"plateNo\\\":\\\"aaa\\\"}\"}";
        Assert.assertEquals(cmd.toJsonDP(), jsonDp);
    }

    @Test
    public void lockCarCmdBuilder() {
        ICmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().lockCarCmdBuilder()
                .withPlateNo("aa")
                .withLockType("add_lock")
                .build();

        System.out.println(JSON.toJSONString(cmd));
        System.out.println(cmd.toJsonDP());
        String jsonDp = "{1:\"{\\\"deviceId\\\":\\\"aa\\\",\\\"gateway\\\":true,\\\"mode\\\":\\\"currentCarQuery\\\"}\"}";
        Assert.assertEquals(cmd.toJsonDP(), jsonDp);
    }

    @Test
    public void currentCarQueryCmdBuilder() {
        ICmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().parkingCurrentCarQueryCmdBuilder()
                .withDeviceId("aa")
                .build();

        System.out.println(JSON.toJSONString(cmd));
        System.out.println(cmd.toJsonDP());
        String jsonDp = "{1:\"{\\\"deviceId\\\":\\\"aa\\\",\\\"gateway\\\":true,\\\"mode\\\":\\\"currentCarQuery\\\"}\"}";
        Assert.assertEquals(cmd.toJsonDP(), jsonDp);
    }

    @Test
    public void parkingLotCmdBuilder() {
        ICmd cmd = CmdBuilderFactory.parkingAreaCmdBuilders().parkingLotCmdBuilder().build();
        System.out.println(JSON.toJSONString(cmd));
        System.out.println(cmd.toJsonDP());
        String dpJson = "{1:\"{\\\"gateway\\\":true,\\\"mode\\\":\\\"uploadParkingLot\\\",\\\"type\\\":\\\"upload\\\"}\"}";
        Assert.assertEquals(dpJson, cmd.toJsonDP());
    }
}
