package cmdissue;


/**
 * @author patrickkk
 * @date 2020/4/13
 */
@FunctionalInterface
public interface ICmdGroupIssueWay {

    /**
     * 指令组下发指令
     *
     * @param cmd
     */
    void dpPublish(ICmd cmd);

}
