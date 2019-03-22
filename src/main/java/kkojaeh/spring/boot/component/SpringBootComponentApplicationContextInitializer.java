package kkojaeh.spring.boot.component;

import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.MethodMetadata;

public class SpringBootComponentApplicationContextInitializer implements ApplicationContextInitializer {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    if (applicationContext.getParent() == null) {
      return;
    }
    val definition = SpringBootComponentDefinition.from(applicationContext);
    if (applicationContext instanceof DefaultListableBeanFactory) {
      val beanFactory = (DefaultListableBeanFactory) applicationContext;
      beanFactory.setAutowireCandidateResolver(
        new TakeContextAnnotationAutowireCandidateResolver(definition));
    } else if (applicationContext instanceof GenericApplicationContext) {
      val beanFactory = ((GenericApplicationContext) applicationContext)
        .getDefaultListableBeanFactory();
      beanFactory.setAutowireCandidateResolver(
        new TakeContextAnnotationAutowireCandidateResolver(definition));
    }
    applicationContext.addApplicationListener(
      new GaveBeanDetectApplicationListener(applicationContext, definition));
  }

  @RequiredArgsConstructor
  private static class TakeContextAnnotationAutowireCandidateResolver extends
    ContextAnnotationAutowireCandidateResolver {

    @NonNull
    private final SpringBootComponentDefinition definition;

    @Override
    protected boolean isLazy(DependencyDescriptor descriptor) {
      boolean lazy = super.isLazy(descriptor);
      if (lazy) {
        for (val ann : descriptor.getAnnotations()) {
          val take = AnnotationUtils.getAnnotation(ann, Take.class);
          if (take != null && take.required()) {
            definition.addTake(descriptor.getDependencyType());
            return true;
          }
        }
        val methodParam = descriptor.getMethodParameter();
        if (methodParam != null) {
          val method = methodParam.getMethod();
          if (method == null || void.class == method.getReturnType()) {
            val take = AnnotationUtils
              .getAnnotation(methodParam.getAnnotatedElement(), Take.class);
            if (take != null && take.required()) {
              definition.addTake(descriptor.getDependencyType());
              return true;
            }
          }
        }
      }
      return lazy;
    }

  }

  @RequiredArgsConstructor
  public class GaveBeanDetectApplicationListener implements
    ApplicationListener<ApplicationReadyEvent> {

    @NonNull
    private final ConfigurableApplicationContext applicationContext;

    @NonNull
    private final SpringBootComponentDefinition definition;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
      val parentContext = (ConfigurableApplicationContext) applicationContext
        .getParent();
      if (parentContext == null) {
        return;
      }
      val beanFactory = applicationContext.getBeanFactory();

      Stream.concat(
        Stream.of(beanFactory.getBeanDefinitionNames())
          .filter(beanDefinitionName -> {
            val beanDefinition = beanFactory
              .getBeanDefinition(beanDefinitionName);

            if (beanDefinition.getSource() instanceof MethodMetadata) {
              return ((MethodMetadata) beanDefinition.getSource())
                .isAnnotated(Give.class.getName());
            }
            return false;
          }),
        Stream.of(beanFactory.getBeanNamesForAnnotation(Give.class))
      ).forEach(name -> {
        Object bean = beanFactory.getBean(name);
        definition.addGive(bean.getClass());
        if (!bean.getClass().isEnum()) {
          bean = ProxyFactory.getProxy(new SingletonTargetSource(bean));
        }
        parentContext.getBeanFactory()
          .registerSingleton(String.format("%s/%s", definition.getName(), name), bean);
      });

      applicationContext.getBean(ApplicationEventMulticaster.class)
        .removeApplicationListener(this);
    }
  }

}
