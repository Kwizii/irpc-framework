package github.kwizii.spring;

import github.kwizii.annotation.IRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Arrays;
import java.util.Set;

@Slf4j
public class IRpcScanner extends ClassPathBeanDefinitionScanner {

    public IRpcScanner(BeanDefinitionRegistry registry) {
        super(registry);
        super.addIncludeFilter(new AnnotationTypeFilter(IRpcService.class));
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            log.warn("No bean defined in '{}' package.", Arrays.toString(basePackages));
        } else {
            log.info("Scan {} bean definition in '{}' package", beanDefinitions.size(), Arrays.toString(basePackages));
        }
        return beanDefinitions;
    }
}
