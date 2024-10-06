package github.kwizii.config;

import github.kwizii.factory.SingletonFactory;
import github.kwizii.registry.zk.CuratorUtil;
import github.kwizii.registry.zk.ZkClientFactory;
import github.kwizii.util.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * When the server  is closed, do something such as unregister all services
 *
 * @author shuang.kou
 * @createTime 2020年06月04日 13:11:00
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();
    private final IRpcSettings rpcSettings;

    public CustomShutdownHook() {
        this.rpcSettings = SingletonFactory.getInstance(IRpcSettings.class);
    }

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("add ShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtil.clearRegistry(ZkClientFactory.getInstance(), rpcSettings.getSocketAddress());
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }
}
