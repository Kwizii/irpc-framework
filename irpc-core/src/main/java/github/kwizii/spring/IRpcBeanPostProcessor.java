package github.kwizii.spring;

import github.kwizii.annotation.IRpcReference;
import github.kwizii.annotation.IRpcService;
import github.kwizii.config.IRpcServiceConfig;
import github.kwizii.config.IRpcSettings;
import github.kwizii.extension.ExtensionLoader;
import github.kwizii.factory.SingletonFactory;
import github.kwizii.provider.ServiceProvider;
import github.kwizii.proxy.IRpcClientProxy;
import github.kwizii.transport.support.TransportSupport;
import github.kwizii.util.PropertiesUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Properties;

@Slf4j
@Component
public class IRpcBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final TransportSupport rpcClient;

    public IRpcBeanPostProcessor() {
        IRpcSettings rpcSettings = loadProperties();
        this.serviceProvider = ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension(rpcSettings.getServiceProvider().getName());
        this.rpcClient = ExtensionLoader.getExtensionLoader(TransportSupport.class).getExtension(rpcSettings.getTransportSupport().getName());
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(IRpcService.class)) {
            log.info("A rpc service provider found: {}", bean.getClass().getName());
            IRpcService anno = bean.getClass().getAnnotation(IRpcService.class);
            IRpcServiceConfig rpcServiceConfig = IRpcServiceConfig.builder()
                    .group(anno.group())
                    .version(anno.version())
                    .service(bean)
                    .build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            IRpcReference rpcReference = field.getAnnotation(IRpcReference.class);
            if (rpcReference != null) {
                IRpcServiceConfig rpcServiceConfig = IRpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version())
                        .build();
                IRpcClientProxy rpcClientProxy = new IRpcClientProxy(rpcClient, rpcServiceConfig);
                Object proxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    // NOTHING
                }
            }
        }
        return bean;
    }

    @SneakyThrows
    private IRpcSettings loadProperties() {
        IRpcSettings rpcSettings = SingletonFactory.getInstance(IRpcSettings.class);
        Properties defaultProperties = PropertiesUtil.get("irpc.default.properties");
        Properties customProperties = PropertiesUtil.get("irpc.properties");
        defaultProperties.putAll(customProperties);
        rpcSettings.load(defaultProperties);
        return rpcSettings;
    }
}
