package github.kwizii.transport.support.netty.server;

import github.kwizii.enums.CompressTypeEnum;
import github.kwizii.enums.SerializationTypeEnum;
import github.kwizii.factory.SingletonFactory;
import github.kwizii.remoting.constant.IRpcConstant;
import github.kwizii.remoting.dto.IRpcMessage;
import github.kwizii.remoting.dto.IRpcRequest;
import github.kwizii.remoting.dto.IRpcResponse;
import github.kwizii.remoting.handler.IRpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final IRpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(IRpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof IRpcMessage) {
                IRpcMessage recvRpcMessage = (IRpcMessage) msg;
                byte messageType = recvRpcMessage.getMessageType();
                IRpcMessage sendRpcMessage = IRpcMessage.builder()
                        .codec(SerializationTypeEnum.HESSIAN.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .build();
                if (messageType == IRpcConstant.HEARTBEAT_REQUEST_TYPE) {
                    log.trace("netty server receive heartbeat message: [{}]", msg);
                    sendRpcMessage.setMessageType(IRpcConstant.HEARTBEAT_RESPONSE_TYPE);
                    sendRpcMessage.setData(IRpcConstant.PONG);
                } else {
                    log.info("netty server receive request message: [{}]", msg);
                    IRpcRequest rpcRequest = (IRpcRequest) recvRpcMessage.getData();
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info("rpc request handler result: [{}]", result);
                    sendRpcMessage.setMessageType(IRpcConstant.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        IRpcResponse<Object> rpcResponse = IRpcResponse.success(rpcRequest.getRequestId(), result);
                        sendRpcMessage.setData(rpcResponse);
                    } else {
                        IRpcResponse<Object> rpcResponse = IRpcResponse.error(rpcRequest.getRequestId());
                        sendRpcMessage.setData(rpcResponse);
                        log.error("drop message, channel is not writable now");
                    }
                }
                ctx.writeAndFlush(sendRpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("netty server catch exception", cause);
        ctx.close();
    }
}
