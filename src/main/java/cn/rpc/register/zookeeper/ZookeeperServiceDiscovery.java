package cn.rpc.register.zookeeper;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.rpc.common.util.CollectionUtil;
import cn.rpc.register.ServiceDiscovery;

/**
 * 基于zookeeper 的服务发现接口实现
 * 
 * @author Administrator
 *
 */

public class ZookeeperServiceDiscovery implements ServiceDiscovery {

	private static final Logger log = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

	private String zkAddress;

	public ZookeeperServiceDiscovery(String registryAddress) {
		this.zkAddress = registryAddress;
	}

	public ZookeeperServiceDiscovery() {
		System.out.println("--------=====-----");
	}

	@Override
	public String discover(String serviceName) {
		// 创建Zookeeper客户端
		ZkClient zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
		log.info("connect zookeeper");
		try {
			String servicePath = Constant.ZK_REGISTRY_PATH + "/" + serviceName;
			if (!zkClient.exists(servicePath)) {
				throw new RuntimeException(String.format("cant not find any service node on path:%s", servicePath));
			}
			List<String> addressList = zkClient.getChildren(servicePath);
			if (CollectionUtil.isEmpty(addressList)) {
				throw new RuntimeException(String.format("can not find any address node on path:%s", servicePath));
			}
			// 获取address 节点
			String address;
			int size = addressList.size();
			if (size == 1) {
				// 若只有一个地址，则获取改地址
				address = addressList.get(0);
				log.info("get only address node:{}", address);
			} else {
				// 若存在多个地址，则随机获取一个地址
				address = addressList.get(ThreadLocalRandom.current().nextInt(size));
				log.info("get address node:{}", address);
			}
			// 获取address节点值
			String addressPath = servicePath + "/" + address;
			return zkClient.readData(addressPath);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			zkClient.close();
		}
		return null;
	}

}
