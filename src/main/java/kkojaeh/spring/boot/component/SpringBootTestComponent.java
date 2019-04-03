package kkojaeh.spring.boot.component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SpringBootTestComponent {

  Class<?> parent() default SpringBootComponentBuilder.NoOpParent.class;

  Class<?>[] siblings() default {};

  Class<? extends Supplier<Class<?>[]>> siblingsSupplier() default NoOpSiblingsSupplier.class;

  class NoOpSiblingsSupplier implements Supplier<Class<?>[]> {

    @Override
    public Class<?>[] get() {
      return new Class[0];
    }
  }

}
