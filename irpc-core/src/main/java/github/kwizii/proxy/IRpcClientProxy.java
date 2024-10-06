package github.kwizii.proxy;

import github.kwizii.config.IRpcServiceConfig;
import github.kwizii.enums.IRpcErrorEnum;
import github.kwizii.enums.IRpcResponseCodeEnum;
import github.kwizii.exception.IRpcException;
import github.kwizii.remoting.dto.IRpcRequest;
import github.kwizii.remoting.dto.IRpcResponse;
import github.kwizii.transport.support.TransportSupport;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

@Slf4j
public class IRpcClientProxy implements InvocationHandler {

    private final TransportSupport rpcClient;
    private final IRpcServiceConfig rpcServiceConfig;

    public IRpcClientProxy(TransportSupport rpcClient, IRpcServiceConfig rpcServiceConfig) {
        this.rpcClient = rpcClient;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoke method: [{}]", method.getName());
        IRpcRequest rpcRequest = IRpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .paramValues(args)
                .build();
        IRpcResponse<Object> rpcResponse = rpcClient.sendRpcRequest(rpcRequest);
        this.check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }

    private void check(IRpcRequest rpcRequest, IRpcResponse<Object> rpcResponse) {
        if (rpcResponse == null) {
            throw new IRpcException(IRpcErrorEnum.SERVICE_INVOCATION_FAILURE, rpcRequest.getInterfaceName());
        }
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new IRpcException(IRpcErrorEnum.MESSAGE_NOT_MATCH, rpcRequest.getInterfaceName());
        }
        if (rpcResponse.getCode() == null || !IRpcResponseCodeEnum.SUCCESS.getCode().equals(rpcResponse.getCode())) {
            throw new IRpcException(IRpcErrorEnum.SERVICE_INVOCATION_FAILURE, rpcRequest.getInterfaceName());
        }
    }
}
