package cn.rpc.server;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import cn.rpc.common.bean.RpcRequest;
import cn.rpc.common.bean.RpcResponse;
import cn.rpc.common.util.StringUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * RPC服务端处理器（用于处理RPC请求）
 * 
 * @author Administrator
 *
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private static final Logger log = LoggerFactory.getLogger(RpcServerHandler.class);

	private final Map<String, Object> handlerMap;

	public RpcServerHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
		// 创建并初始化RPC响应对象
		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());
		try {
			Object result = handle(request);
			response.setResult(result);
		} catch (Exception e) {
			log.info("handle result failure", e);
			response.setException(e);
		}
		// 写入RPC响应对象并自动关闭连接
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private Object handle(RpcRequest request) throws Exception {
		// 获取服务对象
		String serviceName = request.getInterfaceName();
		String serviceVersion = request.getServiceVersion();
		if (StringUtil.isNotEmpty(serviceVersion)) {
			serviceName += "-" + serviceVersion;
		}
		Object serviceBean = handlerMap.get(serviceName);
		if (serviceBean == null) {
			throw new RuntimeException(String.format("can not find service bean by key:%s", serviceName));
		}
		// 获取反射调用所需的参数
		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		// 执行反射调用
		// Method method = serviceClass.getMethod(methodName, parameterTypes);
		// method.setAccessible(true);
		// return method.invoke(serviceBean, parameters);
		// cglib
		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
		return serviceFastMethod.invoke(serviceBean, parameters);
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.info("server caught exception", cause);
		ctx.close();
	}
}
