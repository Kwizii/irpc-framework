package github.kwizii.transport.support.netty.client;

import github.kwizii.config.IRpcSettings;
import github.kwizii.enums.ServiceRegistryEnum;
import github.kwizii.extension.ExtensionLoader;
import github.kwizii.factory.SingletonFactory;
import github.kwizii.registry.ServiceRegistry;
import github.kwizii.remoting.constant.IRpcConstant;
import github.kwizii.remoting.dto.IRpcMessage;
import github.kwizii.remoting.dto.IRpcRequest;
import github.kwizii.remoting.dto.IRpcResponse;
import github.kwizii.transport.codec.IRpcMessageDecoder;
import github.kwizii.transport.codec.IRpcMessageEncoder;
import github.kwizii.transport.support.TransportSupport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcClientSupport implements TransportSupport {

    private final ServiceRegistry serviceRegistry;
    private final UnprocessedRequests unprocessedRequests;
    private final Map<String, Channel> cachedChannel;
    private final Bootstrap bootstrap;
    private final IRpcSettings rpcSettings;

    public NettyRpcClientSupport() {
        this.rpcSettings = SingletonFactory.getInstance(IRpcSettings.class);
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.cachedChannel = new ConcurrentHashMap<>();
        this.bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new IRpcMessageEncoder());
                        p.addLast(new IRpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
    }

    @SneakyThrows
    @Override
    public IRpcResponse<Object> sendRpcRequest(IRpcRequest rpcRequest) {
        CompletableFuture<IRpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            IRpcMessage rpcMessage = IRpcMessage.builder()
                    .codec(rpcSettings.getTransportSerializer().getCode())
                    .compress(rpcSettings.getTransportCompress().getCode())
                    .messageType(IRpcConstant.REQUEST_TYPE)
                    .data(rpcRequest)
                    .build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("netty client send rpc message: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("rpc message send error", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture.get();
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = cachedChannel.get(inetSocketAddress.toString());
        if (channel == null || !channel.isActive()) {
            channel = doConnect(inetSocketAddress);
            cachedChannel.put(inetSocketAddress.toString(), channel);
        }
        return channel;
    }

    @SneakyThrows
    private Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("Netty client connecting to [{}] success", inetSocketAddress.toString());
                channelFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return channelFuture.get();
    }
}
