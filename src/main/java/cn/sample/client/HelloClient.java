package cn.sample.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.rpc.client.RpcProxy;
import cn.sample.api.HelloService;

public class HelloClient {

	public static void main(String[] args) {
		System.out.println("-----");
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-client.xml");
		RpcProxy rpcProxy = context.getBean(RpcProxy.class);

		HelloService helloService = rpcProxy.create(HelloService.class);
		String result = helloService.hello(" World");
		System.out.println(result);
		System.exit(0);
	}

}
