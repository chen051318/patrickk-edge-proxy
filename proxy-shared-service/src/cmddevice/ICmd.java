package cmddevice;

/**
 * <pre>
 * <li>sn属于必传字段</li>
 * <li>
 *  默认1.0版本,数据格式
 *  {
 *     "t":1573557869,
 *     "data": {
 *         "cid": 子设备的本地唯一id，
 *         "dps": {
 *                 "dp编号": {dp内容，根据dp点不不同⽽而不不同},
 *                 "dp编号": {dp内容，根据dp点不不同⽽而不不同}
 *                }
 *     }
 * }
 * </li>
 *
 * <li>
 *   2.0版本, 数据格式
 *  {
 *     "t":1573557869,
 *     "data": {
 *         "cid": 子设备的本地唯一id
 *         “sn”:1112223333,
 *         "dps": {
 *                 "dp编号": {dp内容，根据dp点不不同⽽而不不同},
 *                 "dp编号": {dp内容，根据dp点不不同⽽而不不同}
 *          }
 *     }
 * }
 * </li>
 * </pre>
 *
 * @author patrickkk  2020/10/23 18:01
 */
public interface ICmd extends ICmdFormatter {

    /**
     * 指令对应的dpid
     *
     * @return
     */
    Integer getDpid();

    void setDpid(Integer dpid);

    /**
     * 每个指令对象都有唯一的sn号
     *
     * @return
     */
    Long getSn();

    void setSn(Long sn);

    /**
     * 业务sn号
     *
     * @return
     */
    String getBsn();

    /**
     * 业务sn号
     *
     * @param bsn
     */
    void setBsn(String bsn);

    /**
     * 前置sn号
     *
     * @return
     */
    String getPreSn();

    /**
     * 前置sn号
     *
     * @param preSn
     */
    void setPreSn(String preSn);

    /**
     * 前置sn号
     *
     * @return
     */
    Boolean getRetryEnable();


    Object getData();

    /**
     * true是面向网关的指令，false是面向设备的指令
     *
     * @return
     */
    Boolean getGateway();

    void setGateway(Boolean gateway);

    String getReqType();

    void setReqType(String reqType);

    void setUserBizType(Integer userBizType);

    Integer getUserBizType();

    /**
     * 默认1.0版本
     *
     * @return
     */
    String getCmdVersion();

    void setCmdVersion(String cmdVersion);

    String getEncodeMode();

    void setEncodeMode(String encodeMode);

    String getUid();

}
