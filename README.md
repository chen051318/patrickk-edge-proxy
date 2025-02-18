# gateway-proxy 边缘网关代理
## 项目架构
- atop     设备上传数据处理模块
- client   DP点定义模块
- common   公共使用模块
- core     数据库操作模块，定义DAO类，DO类
- manager  数据层与业务层转换模块，定义DTO数据类
- service  业务系统模块
- shared-service 设备具体业务模块
- starter  启动模块
- test     测试模块

## 新接入设备流程
- 先定义新 DP 点
  在 com.tuya.edgegateway.client.domain.ndp 下
  - dc 表示门禁
  - pa 表示车闸
  - ec 表示梯控
  - ota 表示over the air 无线数据传输
- CMD 指令下发
  在 com.tuya.edgegateway.client 下

- Wiki 文档更新

-本地启动配置
  1.edit Configurations
  2.配置环境参数：
  VM options=-Denv=dev
  Environment variables=PROJECT_NAME=edge-gateway-proxy;zone=a
