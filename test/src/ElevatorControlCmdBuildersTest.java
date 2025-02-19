import com.alibaba.fastjson.JSON;
import com.x.edgegateway.client.domain.ndp.CmdBuilderFactory;
import com.x.edgegateway.client.domain.ndp.common.ICmd;
import com.x.edgegateway.client.domain.ndp.ec.cmd.ElevatorControlCmd;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ElevatorControlCmdBuilders Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>12月 4, 2019</pre>
 */
public class ElevatorControlCmdBuildersTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: elevatorControlCmdBuilder()
     */
    @Test
    public void testElevatorControlCmdBuilder() throws Exception {
        ElevatorControlCmd elevatorControlCmd = CmdBuilderFactory.elevatorControlCmdBuilders().elevatorControlCmdBuilder()
                .withBsn("bsn")
                .withStartFloor(13)
                .withEndFloor(1)
                .withType("type")
                .build();
        Assert.assertEquals(elevatorControlCmd.getDpid(), new Integer(2));
        System.out.println(JSON.toJSONString(elevatorControlCmd.toJsonDP()));
    }

    @Test
    public void testElevatorControlCmdBuilders() throws Exception {
        ICmd cmd0 = CmdBuilderFactory.elevatorControlCmdBuilders()
                .issueElevatorUserInfoAddCmdBuilder()
                .dataBuilder()
                .withUid("uid1")
                .withIdCard("idCard1")
                .withName("name1")
                .withPhone("phone1")
                .withBeginTime(123L)
                .withEndTime(333L)
                .parentBuilder().build();
        System.out.println(JSON.toJSONString(cmd0.toJsonDP()));
    }


    @Test
    public void testElevatorCardAddCmdBuilders() throws Exception {
        ICmd cmd0 = CmdBuilderFactory.elevatorControlCmdBuilders()
                .issueElevatorCardInfoAddCmdBuilder()
                .dataBuilder()
                .withUid("uid1")
                .withCardNo("cardNo1")
                .parentBuilder().build();
        System.out.println(JSON.toJSONString(cmd0.toJsonDP()));
    }

    @Test
    public void testElevatorCardDelCmdBuilders() throws Exception {
        ICmd cmd0 = CmdBuilderFactory.elevatorControlCmdBuilders()
                .issueElevatorCardInfoDelCmdBuilder()
                .dataBuilder()
                .withUid("uid1")
                .withCardNo("cardNo1")
                .parentBuilder().build();
        System.out.println(JSON.toJSONString(cmd0.toJsonDP()));
    }

    @Test
    public void testElevatorCardFreezeCmdBuilders() throws Exception {
        ICmd cmd0 = CmdBuilderFactory.elevatorControlCmdBuilders()
                .issueElevatorCardInfoFreezeCmdBuilder()
                .dataBuilder()
                .withUid("uid1")
                .withCardNo("cardNo1")
                .parentBuilder().build();
        System.out.println(JSON.toJSONString(cmd0.toJsonDP()));
    }

    @Test
    public void testElevatorCardUnfreezeCmdBuilders() throws Exception {
        ICmd cmd0 = CmdBuilderFactory.elevatorControlCmdBuilders()
                .issueElevatorCardInfoUnfreezeCmdBuilder()
                .dataBuilder()
                .withUid("uid1")
                .withCardNo("cardNo1")
                .parentBuilder().build();
        System.out.println(JSON.toJSONString(cmd0.toJsonDP()));
    }

}
