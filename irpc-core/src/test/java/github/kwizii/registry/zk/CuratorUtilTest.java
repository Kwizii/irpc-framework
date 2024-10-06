package github.kwizii.registry.zk;

import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;

import java.util.List;

class CuratorUtilTest {

    @Test
    void createNode() {
    }

    @Test
    void getChildNodes() {
    }

    @Test
    void getZkClient() {
        CuratorFramework zkClient = ZkClientFactory.getInstance();
        System.out.println(zkClient.getCurrentConfig());
        CuratorUtil.createNode(zkClient, "/test", true);
        List<String> nodes = CuratorUtil.getChildNodes(zkClient, "test");
        System.out.println(nodes);
    }

    @Test
    void clearRegistry() {
    }
}