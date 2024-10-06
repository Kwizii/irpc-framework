package github.kwizii.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class CuratorUtil {
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    public static void createNode(CuratorFramework zkClient, String path, Boolean persistence) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("Zookeeper node [{}] already exists", path);
            } else {
                zkClient.create()
                        .creatingParentsIfNeeded()
                        .withMode(persistence ? CreateMode.PERSISTENT : CreateMode.EPHEMERAL)
                        .forPath(path);
                REGISTERED_PATH_SET.add(path);
                log.info("New zookeeper node [{}] created", path);
            }
        } catch (Exception e) {
            log.error("Create zookeeper node for path [{}] error", path);
        }
    }

    public static List<String> getChildNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        try {
            result = zkClient.getChildren().forPath(rpcServiceName);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(zkClient, rpcServiceName);
        } catch (Exception e) {
            log.error("Get child nodes for [{}] failed", rpcServiceName);
        }
        return result;
    }

    private static void registerWatcher(CuratorFramework zkClient, String rpcServiceName) {
        CuratorCache cache = CuratorCache.build(zkClient, rpcServiceName);
        CuratorCacheListener listener = CuratorCacheListener.builder().forChanges((oldNode, node) -> {
            try {
                List<String> serviceAddresses = zkClient.getChildren().forPath(rpcServiceName);
                SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
            } catch (Exception e) {
                log.error("Watcher get child nodes for [{}] failed", rpcServiceName);
            }
        }).build();
        cache.listenable().addListener(listener);
        cache.start();
    }

    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress address) {
        REGISTERED_PATH_SET.parallelStream().forEach(p -> {
            if (p.endsWith(address.toString())) {
                try {
                    zkClient.delete().forPath(p);
                } catch (Exception e) {
                    log.error("Clear registry for path [{}] error", p);
                }
            }
        });
        log.info("All registered services cleared");
    }
}
