package cn.rpc.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import cn.rpc.codec.RpcDecoder;
import cn.rpc.codec.RpcEncoder;
import cn.rpc.common.bean.RpcRequest;
import cn.rpc.common.bean.RpcResponse;
import cn.rpc.common.util.StringUtil;
import cn.rpc.register.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * spring 启动加载
 * 
 * @author Administrator
 *
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(RpcServer.class);

	private String serviceAddress;

	private ServiceRegistry serviceRegistry;

	/**
	 * 存放服务名 、服务对象 之间的映射关系
	 */
	private Map<String, Object> handlerMap = new HashMap<>();

	public RpcServer(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public RpcServer(String serviceAddress, ServiceRegistry serviceRegistry) {
		this.serviceAddress = serviceAddress;
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		// 扫描带有RpcService注解的类，并初始化handlerMap对象
		Map<String, Object> serviceMap = ctx.getBeansWithAnnotation(RpcService.class);
		if (MapUtils.isNotEmpty(serviceMap)) {
			for (Object serviceBean : serviceMap.values()) {
				RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
				String serviceName = rpcService.value().getName();
				String serviceVersion = rpcService.version();
				if (StringUtils.isNotBlank(serviceVersion)) {
					serviceName += "-" + serviceVersion;
				}
				handlerMap.put(serviceName, serviceBean);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast(new RpcDecoder(RpcRequest.class));// 解码RPC请求
					pipeline.addLast(new RpcEncoder(RpcResponse.class));// 编码RPC响应
					pipeline.addLast(new RpcServerHandler(handlerMap));// 处理RPC请求

				}
			});
			bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			// 获取RPC服务器IP地址 和端口号
			String[] addressArray = StringUtil.split(serviceAddress, ":");
			String ip = addressArray[0];
			int port = Integer.parseInt(addressArray[1]);
			// 启动RPC服务器
			ChannelFuture future = bootstrap.bind(ip, port).sync();
			// 注册rpc服务地址
			if (serviceRegistry != null) {
				for (String interfaceName : handlerMap.keySet()) {
					serviceRegistry.register(interfaceName, serviceAddress);
					log.debug("register service: {} => {}", interfaceName, serviceAddress);
				}
			}
			// 关闭 RPC 服务器
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}

	}

}
