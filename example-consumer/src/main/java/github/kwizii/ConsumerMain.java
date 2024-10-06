package github.kwizii;

import github.kwizii.annotation.IRpcScan;
import github.kwizii.transport.support.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@IRpcScan
public class ConsumerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ConsumerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) context.getBean("nettyRpcServer");
        nettyRpcServer.start();
    }
}
