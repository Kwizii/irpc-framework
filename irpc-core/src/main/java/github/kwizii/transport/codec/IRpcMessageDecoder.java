package github.kwizii.transport.codec;

import github.kwizii.enums.CompressTypeEnum;
import github.kwizii.enums.SerializationTypeEnum;
import github.kwizii.extension.ExtensionLoader;
import github.kwizii.remoting.constant.IRpcConstant;
import github.kwizii.remoting.dto.IRpcMessage;
import github.kwizii.remoting.dto.IRpcRequest;
import github.kwizii.remoting.dto.IRpcResponse;
import github.kwizii.transport.compress.Compress;
import github.kwizii.transport.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * custom protocol decoder
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
 * <p>
 * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 * </p>
 *
 * @author wangtao
 * @createTime on 2020/10/2
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */
@Slf4j
public class IRpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public IRpcMessageDecoder() {
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(IRpcConstant.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength      Maximum frame length. It decide the maximum length of data that can be received.
     *                            If it exceeds, the data will be discarded.
     * @param lengthFieldOffset   Length field offset. The length field is the one that skips the specified length of byte.
     * @param lengthFieldLength   The number of bytes in the length field.
     * @param lengthAdjustment    The compensation value to add to the value of the length field
     * @param initialBytesToStrip Number of bytes skipped.
     *                            If you need to receive all of the header+body data, this value is 0
     *                            if you only want to receive the body data, then you need to skip the number of bytes consumed by the header.
     */
    public IRpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                              int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= IRpcConstant.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }

        }
        return decoded;
    }


    private Object decodeFrame(ByteBuf in) {
        // note: must read ByteBuf in order
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        // build RpcMessage object
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        IRpcMessage rpcMessage = IRpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        if (messageType == IRpcConstant.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(IRpcConstant.PING);
            return rpcMessage;
        }
        if (messageType == IRpcConstant.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(IRpcConstant.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - IRpcConstant.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // decompress the bytes
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bs = compress.decompress(bs);
            // deserialize the object
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            if (messageType == IRpcConstant.REQUEST_TYPE) {
                IRpcRequest tmpValue = serializer.deserialize(bs, IRpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                IRpcResponse tmpValue = serializer.deserialize(bs, IRpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;

    }

    private void checkVersion(ByteBuf in) {
        // read the version and compare
        byte version = in.readByte();
        if (version != IRpcConstant.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = IRpcConstant.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != IRpcConstant.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }

}
