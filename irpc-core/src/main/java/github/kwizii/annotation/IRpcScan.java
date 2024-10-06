package github.kwizii.annotation;


import github.kwizii.spring.IRpcScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(IRpcScannerRegistrar.class)
@Documented
public @interface IRpcScan {
    String[] value() default {};

    String[] basePackages() default {};
}
