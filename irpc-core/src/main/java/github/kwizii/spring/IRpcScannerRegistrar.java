package github.kwizii.spring;

import github.kwizii.annotation.IRpcScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class IRpcScannerRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes irpcScanAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(IRpcScan.class.getName()));
        if (irpcScanAttrs != null) {
            registerBeanDefinitions(importingClassMetadata, irpcScanAttrs, registry);
        }
    }

    private void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, AnnotationAttributes irpcScanAttrs, BeanDefinitionRegistry registry) {
        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(Arrays.stream(irpcScanAttrs.getStringArray("value")).filter(StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(irpcScanAttrs.getStringArray("basePackages")).filter(StringUtils::hasText).collect(Collectors.toList()));
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        IRpcScanner serviceScanner = new IRpcScanner(registry);
        // 注册BeanDefinition
        serviceScanner.scan(StringUtils.collectionToCommaDelimitedString(basePackages));
    }
}
