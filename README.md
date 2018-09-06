# rpcsrc  简单rpc实现，来源于网络

## 执行顺序：

### 1、首先修改配置文件:rpc.properties|spring.xml 为server端配置文件；config.properties|spring-client.xml为客户端配置文件，修改相应的参数值
### 2、执行RpcBootstrap类的main方法，初始化服务端spring，启动netty服务端链接
### 3、执行HelloClient类的main方法，即可看到调用结果
