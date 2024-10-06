package github.kwizii.transport.support.netty.client;

import github.kwizii.enums.CompressTypeEnum;
import github.kwizii.enums.SerializationTypeEnum;
import github.kwizii.factory.SingletonFactory;
import github.kwizii.remoting.constant.IRpcConstant;
import github.kwizii.remoting.dto.IRpcMessage;
import github.kwizii.remoting.dto.IRpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClientSupport rpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.rpcClient = SingletonFactory.getInstance(NettyRpcClientSupport.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof IRpcMessage) {
                IRpcMessage castMsg = (IRpcMessage) msg;
                byte type = castMsg.getMessageType();
                if (type == IRpcConstant.HEARTBEAT_RESPONSE_TYPE) {
                    if (log.isTraceEnabled()) {
                        log.trace("client received a heartbeat response: [{}]", castMsg.getData());
                    }
                } else if (type == IRpcConstant.RESPONSE_TYPE) {
                    log.trace("client received a rpc response: [{}]", castMsg.getData());
                    IRpcResponse<Object> rpcResponse = (IRpcResponse<Object>) castMsg.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                if (log.isTraceEnabled()) {
                    log.trace("write idle happen [{}]", ctx.channel());
                }
                Channel channel = rpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                IRpcMessage rpcMessage = IRpcMessage.builder()
                        .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .messageType(IRpcConstant.HEARTBEAT_REQUEST_TYPE)
                        .data(IRpcConstant.PING)
                        .build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("netty client catch exception: ", cause);
        ctx.close();
    }
}
