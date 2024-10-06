package github.kwizii.transport.support;


import github.kwizii.extension.SPI;
import github.kwizii.remoting.dto.IRpcRequest;
import github.kwizii.remoting.dto.IRpcResponse;

@SPI
public interface TransportSupport {
    IRpcResponse<Object> sendRpcRequest(IRpcRequest rpcRequest);
}
