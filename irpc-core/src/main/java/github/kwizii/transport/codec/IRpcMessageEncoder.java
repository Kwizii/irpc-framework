package github.kwizii.transport.codec;


import github.kwizii.enums.CompressTypeEnum;
import github.kwizii.enums.SerializationTypeEnum;
import github.kwizii.extension.ExtensionLoader;
import github.kwizii.remoting.constant.IRpcConstant;
import github.kwizii.remoting.dto.IRpcMessage;
import github.kwizii.transport.compress.Compress;
import github.kwizii.transport.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 *
 * @author WangTao
 * @createTime on 2020/10/2
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */

@Slf4j
public class IRpcMessageEncoder extends MessageToByteEncoder<IRpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, IRpcMessage rpcMessage, ByteBuf out) {
        try {
            out.writeBytes(IRpcConstant.MAGIC_NUMBER);
            out.writeByte(IRpcConstant.VERSION);
            // leave a place to write the value of full length
            out.writerIndex(out.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            out.writeByte(rpcMessage.getCodec());
            out.writeByte(rpcMessage.getCompress());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            // build full length
            byte[] bodyBytes = null;
            int fullLength = IRpcConstant.HEAD_LENGTH;
            // if messageType is not heartbeat message,fullLength = head length + body length
            if (messageType != IRpcConstant.HEARTBEAT_REQUEST_TYPE
                    && messageType != IRpcConstant.HEARTBEAT_RESPONSE_TYPE) {
                // serialize the object
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                // compress the bytes
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }
            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writeIndex = out.writerIndex();
            out.writerIndex(writeIndex - fullLength + IRpcConstant.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}

