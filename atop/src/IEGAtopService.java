import com.tuya.arthas.client.domain.vo.datapoint.SchemaAnalyzeVO;
import com.tuya.atop.client.domain.api.ApiRequestDO;
import com.tuya.atop.client.domain.common.AtopResult;
import com.tuya.edgegateway.atop.model.vo.DeviceVO;
import com.tuya.edgegateway.atop.model.vo.FaceScoreVO;
import com.tuya.edgegateway.atop.model.vo.GatewayVO;
import com.tuya.edgegateway.atop.model.vo.UploadTokenVO;
import com.tuya.edgegateway.client.common.PageResult;
import com.tuya.edgegateway.client.domain.cmd.IssueDataCmdResponseVO;

import java.util.List;

/**
 * 边缘网关通用atop接口
 *
 * @author: patrickkk  date： 2019/12/4
 */
public interface IEGAtopService {

    /**
     * 网关数据同步
     *
     * @param input        the input
     * @param apiRequestDO the api request do
     * @return atop result
     */
    @Deprecated
    AtopResult<GatewayVO> uploadGateway(String input, ApiRequestDO apiRequestDO);

    /**
     * 接收边缘关网发送的设备同步数据
     *
     * @param jsonData     设备数据集合 false
     * @param apiRequestDO the api request do
     * @return atop result
     * @clientApi tuya.industry.base.eg.device.post
     * @clientVersion 1.0
     * @scribedescribe 接收边缘关网发送的设备同步数据
     * @invokeTimeout 10
     * @sessionRequired false
     */
    AtopResult<Boolean> uploadDevices(String jsonData, ApiRequestDO apiRequestDO);

    /**
     * 上报指令下发（数据同步类）的执行结果
     *
     * @param sn           操作序号，指令下发时给的sn号    false
     * @param success      0:失败；1：成功            false
     * @param message      如果指令执行失败，这里填上失败原因  true
     * @param data         发送的json数据的string表示(需同时相应数据场景) true
     * @param apiRequestDO the api request do
     * @return atop result
     * @clientApi tuya.industry.base.eg.syncdata.result.post
     * @clientVersion 1.0
     * @scribedescribe 上报指令下发 （数据同步类）执行结果
     * @invokeTimeout 5
     * @sessionRequired false
     */
    AtopResult<Boolean> uploadIssueDataCmdResult(Long sn, Integer success, String message, String data, ApiRequestDO apiRequestDO);

    /**
     * 上报指令下发(带业务结果)的执行结果,比如日志上传,会带上传后生成的文件id,通过文件id可以找到文件.
     *
     * @param input        请求id true
     * @param apiRequestDO the api request do
     * @return atop result
     * @clientApi tuya.industry.base.edge.gateway.syncdata.type.result.post
     * @clientVersion 1.0
     * @scribedescribe 接收本地边缘网关上传的文件id
     * @invokeTimeout 2
     * @sessionRequired false
     */
    AtopResult<Boolean> uploadIssueDataCmdResultWithType(String input, ApiRequestDO apiRequestDO);

    /**
     * 根据cid查询设备
     *
     * @param cid          三方设备id  false
     * @param apiRequestDO the api request do
     * @return atop result
     * @appName edge -gateway-proxy
     * @clientApi tuya.industry.base.eg.device.cid.query
     * @clientVersion 1.0
     * @scribedescribe 根据cid查询设备
     * @invokeTimeout 5
     * @sessionRequired false
     */
    AtopResult<DeviceVO> queryDeviceForCid(String cid, ApiRequestDO apiRequestDO);

    /**
     * 查询边缘网关对象
     *
     * @param gatewayId    网关id false
     * @param apiRequestDO the api request do
     * @return atop result
     * @clientApi tuya.industry.base.edge.gateway.id.query
     * @clientVersion 1.0
     * @scribedescribe 根据网关id查询边缘网关对象
     * @invokeTimeout 5
     * @sessionRequired false
     */
    AtopResult<GatewayVO> queryGatewayForGatewayId(String gatewayId, ApiRequestDO apiRequestDO);

    /**
     * 查询边缘网关下的子设备列表
     *
     * @param pageIndex    查询页码    false
     * @param limit        每页行数，最大100条 false
     * @param apiRequestDO the api request do
     * @return atop result
     * @clientApi tuya.industry.base.edge.device.query.all
     * @clientVersion 1.0
     * @scribedescribe 设备查询
     * @invokeTimeout 5
     * @sessionRequired false
     */
    AtopResult<PageResult<DeviceVO>> queryDevice(Integer pageIndex, Integer limit, ApiRequestDO apiRequestDO);


    /**
     * 查询mqtt的下发数据指令流水，做数据同步的事务补偿用
     *
     * @param startCursor  查询开始的游标,首次为0  false
     * @param limit        限制返回行数，最大100    false
     * @param apiRequestDO the api request do
     * @return atop result
     * @clientApi tuya.industry.base.eg.syncdata.query_1.0
     */
    @Deprecated
    AtopResult<List<IssueDataCmdResponseVO>> queryIssueDataCmd(Long startCursor, Integer limit, ApiRequestDO apiRequestDO);

    /**
     * 查询schema
     *
     * @param cid          三方设备id  false
     * @param apiRequestDO the api request do
     * @return atop result
     * @appName edge -gateway-proxy
     * @clientApi tuya.industry.base.eg.device.query.schema
     * @clientVersion 1.0
     * @scribedescribe 查询schema
     * @invokeTimeout 5
     * @sessionRequired false
     */
    AtopResult<List<SchemaAnalyzeVO>> querySchema(String cid, ApiRequestDO apiRequestDO);

    /**
     * 获取文件token
     *
     * @param subjectType  the subject type
     * @param apiRequestDO the api request do
     * @return upload token
     * @clientApi tuya.industry.base.edge.file.upload.token
     * @clientVersion 1.0
     * @scribedescribe 设备查询
     * @invokeTimeout 5
     * @sessionRequired false
     */
    AtopResult<UploadTokenVO> getUploadToken(String subjectType, ApiRequestDO apiRequestDO);

    /**
     * 人脸照片评分
     *
     * @param fileId       上传文件后拿到的ID
     * @param apiRequestDO the api request do
     * @return FaceScoreVO 包括结果和图片url
     * @clientApi tuya.industry.base.edge.ai.face.score
     * @clientVersion 1.0
     * @scribedescribe 评估图片质量
     * @invokeTimeout 5
     * @sessionRequired false
     */
    AtopResult<FaceScoreVO> evaluateFaceQuality(String fileId, ApiRequestDO apiRequestDO);

    /**
     * 上传算法视频的url给到算法那里
     *
     * @return
     * @clientApi tuya.industry.base.edge.file.upload.alg
     * @clientVersion 1.0
     * @scribedescribe 设备查询
     * @invokeTimeout 5
     * @sessionRequired false
     */
    AtopResult<Boolean> uploadAlgInfo(String input, ApiRequestDO apiRequestDO);
}
