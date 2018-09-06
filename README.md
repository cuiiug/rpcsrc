# rpcsrc  简单rpc实现，来源于网络

## 执行顺序：

### 1、首先修改配置文件:rpc.properties|spring.xml 为server端配置文件；config.properties|spring-client.xml为客户端配置文件，修改相应的参数值
### 2、执行RpcBootstrap类的main方法，初始化服务端spring，启动netty服务端链接
### 3、执行HelloClient类的main方法，即可看到调用结果

###==========================================================

## 执行顺序：
###1、服务端启动spring容器，扫描spring.xml配置的bean
###2、cn.rpc.server.RpcServer类实现了ApplicationContextAware, InitializingBean接口
#### 1、setApplicationContext方法通过spring容器，获取所有实现了RpcService注解的类（作为RPC服务端的类），放入handlerMap，key：serviceName；value：类实例
#### 2、afterPropertiesSet方法中，启动netty服务端代码，获取配置文件中的service_address地址，以及zookeeper地址，注册到zookeeper中，启动netty服务端监听
#### 3、netty的handler类RpcServerHandler 处理netty监听的请求，参数类型为RpcRequest
#### 4、RpcServerHandler从RpcRequest类中，获取serviceName，parameter等数据，从handlerMap中获取对应的类，通过放射调用，进行方法执行，返回RpcResponse

### 3、客户端启动

