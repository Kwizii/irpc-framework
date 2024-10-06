package github.kwizii.transport.support.netty.server;

import github.kwizii.config.CustomShutdownHook;
import github.kwizii.config.IRpcSettings;
import github.kwizii.factory.SingletonFactory;
import github.kwizii.transport.codec.IRpcMessageDecoder;
import github.kwizii.transport.codec.IRpcMessageEncoder;
import github.kwizii.util.RuntimeUtil;
import github.kwizii.util.threadpool.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class NettyRpcServer {

    private final IRpcSettings rpcSettings;

    public NettyRpcServer() {
        rpcSettings = SingletonFactory.getInstance(IRpcSettings.class);
    }


    @SneakyThrows
    public void start() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false));
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                        p.addLast(new IRpcMessageEncoder());
                        p.addLast(new IRpcMessageDecoder());
                        p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                    }
                });
        try {
            ChannelFuture future = bootstrap.bind(rpcSettings.getSocketAddress()).sync();
            future.channel().closeFuture().sync();
            log.info("netty server start at {}", rpcSettings.getSocketAddress());
        } catch (InterruptedException e) {
            log.error("start netty server failed: ", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }
}