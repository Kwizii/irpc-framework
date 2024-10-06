package github.kwizii.registry.zk;

import github.kwizii.config.IRpcSettings;
import github.kwizii.factory.SingletonFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ZkClientFactory {
    private static final Integer SESSION_TIMEOUT_MS = 60 * 1000;
    private static final Integer CONNECTION_TIMEOUT_MS = 15 * 1000;

    private ZkClientFactory() {
    }

    public static CuratorFramework getInstance() {
        return ZkClientFactoryHolder.instance;
    }

    static class ZkClientFactoryHolder {
        private static final CuratorFramework instance = createClient();

        private static CuratorFramework createClient() {
            IRpcSettings rpcSettings = SingletonFactory.getInstance(IRpcSettings.class);
            Properties properties = rpcSettings.getProperties();
            ZkSettings zkSettings = SingletonFactory.getInstance(ZkSettings.class);
            zkSettings.load(properties);

            ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(zkSettings.getSleepTime(), zkSettings.getMaxRetires());
            CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                    .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                    .connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
                    .connectString(zkSettings.getAddress())
                    .retryPolicy(retryPolicy)
                    .namespace(zkSettings.getNamespace())
                    .build();
            zkClient.start();
            try {
                if (!zkClient.blockUntilConnected(zkSettings.getMaxWaitTime(), TimeUnit.SECONDS)) {
                    throw new RuntimeException("Connecting to zookeeper timed out");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for the connection of zookeeper error", e);
            }
            return zkClient;
        }
    }
}
