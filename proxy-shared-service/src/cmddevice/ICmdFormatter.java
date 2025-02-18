package cmddevice;

/**
 * @author: patrickkk
 * date： 2019/12/7
 */
public interface ICmdFormatter {

    /**
     * 转换成mqtt消息的dp格式
     * @return
     */
    String toJsonDP();


}
