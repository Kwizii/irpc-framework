package github.kwizii.provider.zk;

import github.kwizii.config.IRpcServiceConfig;
import github.kwizii.config.IRpcSettings;
import github.kwizii.enums.IRpcErrorEnum;
import github.kwizii.exception.IRpcException;
import github.kwizii.extension.ExtensionLoader;
import github.kwizii.factory.SingletonFactory;
import github.kwizii.provider.ServiceProvider;
import github.kwizii.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;
    private final IRpcSettings rpcSettings;

    public ZkServiceProviderImpl() {
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.rpcSettings = SingletonFactory.getInstance(IRpcSettings.class);
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(rpcSettings.getServiceRegistry().getName());
    }

    @Override
    public void cacheService(IRpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            log.debug("Added a duplicated rpc service: {}", rpcServiceName);
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces: {}", rpcServiceName,
                rpcServiceConfig
                        .getService()
                        .getClass()
                        .getInterfaces());
    }

    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new IRpcException(IRpcErrorEnum.SERVICE_NOT_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(IRpcServiceConfig serviceConfig) {
        serviceRegistry.registerService(serviceConfig.getRpcServiceName(), rpcSettings.getSocketAddress());
        this.cacheService(serviceConfig);
    }
}
