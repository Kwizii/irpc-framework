package github.kwizii.provider;

import github.kwizii.config.IRpcServiceConfig;
import github.kwizii.extension.SPI;

@SPI
public interface ServiceProvider {

    void cacheService(IRpcServiceConfig rpcServiceConfig);

    Object getService(String serviceName);

    void publishService(IRpcServiceConfig serviceConfig);
}
