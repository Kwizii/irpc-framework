package github.kwizii.annotation;


import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface IRpcService {

    String version() default "";

    String group() default "";
}
