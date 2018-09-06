package cn.sample.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RpcBootstrap {
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcBootstrap.class);

	public static void main(String[] args) {
		LOGGER.debug("start server");
		// 启动spring容器
		/**
		 * Spring容器启动过程，会加载相应的bean; RpcServer实现了InitializingBean接口，初始化bean时
		 * 执行afterPropertiesSet方法
		 */
		new ClassPathXmlApplicationContext("spring.xml");
	}
}
