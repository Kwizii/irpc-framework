package github.kwizii.registry.zk;

import github.kwizii.util.AbsSettings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ZkSettings extends AbsSettings {
    private String address;
    private int sleepTime;
    private int maxRetires;
    private int maxWaitTime;
    private String namespace;

    public ZkSettings() {
        keys.put("address", "service.registry.zookeeper.address");
        keys.put("sleepTime", "service.registry.zookeeper.sleepTime");
        keys.put("maxRetires", "service.registry.zookeeper.maxRetries");
        keys.put("maxWaitTime", "service.registry.zookeeper.maxWaitTime");
        keys.put("namespace", "service.registry.zookeeper.namespace");
    }
}
