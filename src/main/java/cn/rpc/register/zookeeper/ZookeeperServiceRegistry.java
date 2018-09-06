package cn.rpc.register.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.rpc.register.ServiceRegistry;

public class ZookeeperServiceRegistry implements ServiceRegistry {

	private static final Logger log = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

	private final ZkClient zkClient;

	public ZookeeperServiceRegistry(String zkAddress) {
		zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
		log.info("connect zookeeper");
	}

	@Override
	public void register(String serviceName, String serviceAddress) {
		// 创建registry节点（持久）
		String registryPath = Constant.ZK_REGISTRY_PATH;
		if (!zkClient.exists(registryPath)) {
			zkClient.createPersistent(registryPath);
			log.info("create registry node:{}", registryPath);
		}
		// 创建 service 节点（持久）
		String servicePath = registryPath + "/" + serviceName;
		if (!zkClient.exists(servicePath)) {
			zkClient.createPersistent(servicePath);
			log.info("create service node:{}", servicePath);
		}
		// 创建 address临时节点
		String addressPath = servicePath + "/address-";
		String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
		log.info("create address node:{}", addressNode);
	}

}
