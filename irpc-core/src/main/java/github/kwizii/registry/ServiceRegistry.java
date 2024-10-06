package github.kwizii.registry;

import github.kwizii.extension.SPI;
import github.kwizii.remoting.dto.IRpcRequest;

import java.net.InetSocketAddress;

@SPI
public interface ServiceRegistry {
    void registerService(String serviceName, InetSocketAddress address);

    InetSocketAddress lookupService(IRpcRequest rpcRequest);
}
