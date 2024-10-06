package github.kwizii.config;

import github.kwizii.enums.*;
import github.kwizii.util.AbsSettings;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Getter
@Setter
public final class IRpcSettings extends AbsSettings {

    private String transportServerHost;
    private int transportServerPort;
    private TransportSupportEnum transportSupport;
    private CompressTypeEnum transportCompress;
    private SerializationTypeEnum transportSerializer;
    private ServicepRroviderEnum serviceProvider;
    private ServiceRegistryEnum serviceRegistry;
    private ServiceLoadBalancerEnum serviceLoadbalancer;

    public IRpcSettings() throws UnknownHostException {
        keys.put("transportServerHost", "transport.server.host");
        keys.put("transportServerPort", "transport.server.port");
        keys.put("transportSupport", "transport.support");
        keys.put("transportCompress", "transport.compress");
        keys.put("transportSerializer", "transport.serializer");
        keys.put("serviceProvider", "service.provider");
        keys.put("serviceRegistry", "service.registry");
        keys.put("serviceLoadbalancer", "service.loadbalancer");
        this.transportServerHost = InetAddress.getLocalHost().getHostAddress();
    }

    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(transportServerHost, transportServerPort);
    }
}
