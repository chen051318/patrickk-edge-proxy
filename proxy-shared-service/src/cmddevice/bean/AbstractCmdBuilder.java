package cmddevice.bean;

import cmddevice.ICmd;
import com.google.common.base.Strings;
import com.tuya.edgegateway.client.common.BaseBean;
import com.tuya.edgegateway.client.common.ObjectUtils;
import org.springframework.util.Assert;

/**
 * @author: patrickkk
 * date： 2019/12/12
 */
public abstract class AbstractCmdBuilder<T extends ICmd, TB extends AbstractCmdBuilder<T, TB>> extends BaseBean {

    protected String bsn;

    protected String preSn;

    protected String reqType;

    protected Integer userBizType;

    public TB withBsn(String bsn) {
        this.bsn = bsn;
        return (TB) this;
    }

    public TB withUserBizType(Integer userBizType) {
        this.userBizType = userBizType;
        return (TB) this;
    }

    public TB withRequestType(String reqType) {
        this.reqType = reqType;
        return (TB) this;
    }

    public TB withPreSn(String preSn) {
        this.preSn = preSn;
        return (TB) this;
    }

    public final T build() {
        Class<T> clazz = cmdClass();
        Assert.notNull(clazz, "cmd实例的Class不能为空！");
        T cmd = ObjectUtils.getIntance(clazz);
//        T cmd = ObjectUtils.newInstance(clazz);
//         Assert.hasText(bsn,"业务sn号不能为空！");
        if (Strings.isNullOrEmpty(bsn)) {
            bsn = "";
        }
        appendPreSn(cmd);
        appendBSN(cmd);
        appendReqType(cmd);
        appendUserBizType(cmd);
        addFields(cmd);
        return cmd;
    }

    private void appendPreSn(T cmd) {
        cmd.setPreSn(this.preSn);
    }

    private void appendBSN(T cmd) {
        cmd.setBsn(this.bsn);
    }

    private void appendReqType(T cmd) {
        cmd.setReqType(this.reqType);
    }

    private void appendUserBizType(T cmd) {cmd.setUserBizType(this.userBizType);}

    /**
     * 具体cmd的class
     *
     * @return
     */
    protected abstract Class<T> cmdClass();

    /**
     * @param cmd
     */
    protected abstract void addFields(T cmd);

}
