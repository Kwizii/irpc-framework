package github.kwizii.annotation;


import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface IRpcReference {

    String version() default "";

    String group() default "";
}
