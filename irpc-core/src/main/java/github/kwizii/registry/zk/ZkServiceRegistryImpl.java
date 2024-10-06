package github.kwizii.registry.zk;

import github.kwizii.config.IRpcSettings;
import github.kwizii.enums.IRpcErrorEnum;
import github.kwizii.exception.IRpcException;
import github.kwizii.extension.ExtensionLoader;
import github.kwizii.factory.SingletonFactory;
import github.kwizii.loadbalance.LoadBalance;
import github.kwizii.registry.ServiceRegistry;
import github.kwizii.remoting.dto.IRpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {

    private final LoadBalance loadBalancer;

    public ZkServiceRegistryImpl() {
        IRpcSettings rpcSettings = SingletonFactory.getInstance(IRpcSettings.class);
        this.loadBalancer = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(rpcSettings.getServiceLoadbalancer().getName());
    }

    @Override
    public InetSocketAddress lookupService(IRpcRequest rpcRequest) {
        String serviceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = ZkClientFactory.getInstance();
        List<String> serviceList = CuratorUtil.getChildNodes(zkClient, toZkPath(serviceName));
        if (serviceList == null || serviceList.isEmpty()) {
            throw new IRpcException(IRpcErrorEnum.SERVICE_NOT_FOUND, serviceName);
        }
        String targetServiceUrl = loadBalancer.selectServiceAddress(serviceList, rpcRequest);
        log.info("select service [{}]", targetServiceUrl);
        return url2SocketAddress(targetServiceUrl);
    }

    @Override
    public void registerService(String serviceName, InetSocketAddress address) {
        String servicePath = "/" + serviceName + address.toString();
        CuratorFramework zkClient = ZkClientFactory.getInstance();
        CuratorUtil.createNode(zkClient, servicePath, false);
    }

    private InetSocketAddress url2SocketAddress(String url) {
        String[] strings = url.split(":");
        int port = Integer.parseInt(strings[1]);
        return new InetSocketAddress(strings[0], port);
    }

    private String toZkPath(String path) {
        if (path != null && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }
}
