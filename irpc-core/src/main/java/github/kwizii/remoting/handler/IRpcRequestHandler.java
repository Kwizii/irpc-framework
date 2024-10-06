package github.kwizii.remoting.handler;

import github.kwizii.config.IRpcSettings;
import github.kwizii.exception.IRpcException;
import github.kwizii.extension.ExtensionLoader;
import github.kwizii.factory.SingletonFactory;
import github.kwizii.provider.ServiceProvider;
import github.kwizii.remoting.dto.IRpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class IRpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public IRpcRequestHandler() {
        IRpcSettings rpcSettings = SingletonFactory.getInstance(IRpcSettings.class);
        this.serviceProvider = ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension(rpcSettings.getServiceProvider().getName());
    }

    public Object handle(IRpcRequest request) {
        Object service = serviceProvider.getService(request.getRpcServiceName());
        return invokeTargetService(request, service);
    }

    private Object invokeTargetService(IRpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParamValues());
            log.info("invoke method {}.{} successful", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IRpcException(e.getMessage(), e);
        }
        return result;
    }
}
