package cmddevice.bean;

import cmddevice.ICmd;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.tuya.edgegateway.client.common.BaseBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: patrickkk
 * date： 2019/12/3
 */
public abstract class CmdSupport extends BaseBean implements ICmd {
    /**
     * 操作序列号，每个指令下发时会生成一个，可用于跟踪响应及去重 true
     */
    private Long sn;

    /**
     * 用户id
     */
    private String uid;

    /**
     * 业务端下发时传入的sn号
     */
    @JSONField(serialize = false)
    private String bsn;

    /**
     * 被依赖sn号
     */
    @JSONField(serialize = false)
    private String preSn;

    @JSONField(serialize = false)
    private Integer dpid;

    @JSONField(serialize = false)
    private Boolean retryEnable = Boolean.FALSE;

    /**
     * true是面向网关的指令，false是面向设备的指令
     */
    private Boolean gateway;

    private String reqType;

    private Integer userBizType;

    /**
     * 消息格式版本,默认1.0
     */
    @JSONField(serialize = false)
    private String cmdVersion = "1.0";

    /**
     * 编码模式
     */
    @JSONField(serialize = false)
    private String encodeMode = "default";

    @Override
    public Integer getDpid() {
        return dpid;
    }

    @Override
    public void setDpid(Integer dpid) {
        this.dpid = dpid;
    }

    @Override
    public Boolean getGateway() {
        return gateway;
    }

    @Override
    public void setGateway(Boolean gateway) {
        this.gateway = gateway;
    }

    @Override
    public Long getSn() {
        return sn;
    }

    @Override
    public void setSn(Long sn) {
        this.sn = sn;
    }

    @Override
    public String getBsn() {
        return bsn;
    }

    @Override
    public void setBsn(String bsn) {
        this.bsn = bsn;
    }

    @Override
    public String getReqType() {
        return reqType;
    }

    @Override
    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    @Override
    public void setUserBizType(Integer userBizType) {
        this.userBizType = userBizType;
    }

    @Override
    public Integer getUserBizType() {
        return this.userBizType;
    }

    @Override
    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPreSn() {
        return preSn;
    }

    public void setPreSn(String preSn) {
        this.preSn = preSn;
    }

    @Override
    public String getCmdVersion() {
        return cmdVersion;
    }

    @Override
    public void setCmdVersion(String cmdVersion) {
        this.cmdVersion = cmdVersion;
    }

    @Override
    public Boolean getRetryEnable() {
        return this.retryEnable;
    }

    @Override
    public String getEncodeMode() {
        return encodeMode;
    }

    @Override
    public void setEncodeMode(String encodeMode) {
        this.encodeMode = encodeMode;
    }

    @Override
    public String toJsonDP() {
        Map<Integer, String> encodeStrMap = new HashMap<>();
        String cmdStr = JSON.toJSONString(this);
        encodeStrMap.put(this.getDpid(), cmdStr);
        return JSON.toJSONString(encodeStrMap);
    }

}
