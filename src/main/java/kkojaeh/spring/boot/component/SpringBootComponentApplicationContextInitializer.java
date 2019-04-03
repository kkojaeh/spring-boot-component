package kkojaeh.spring.boot.component;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.MethodMetadata;

public class SpringBootComponentApplicationContextInitializer implements
  ApplicationContextInitializer {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    val definition = SpringBootComponentDefinition.from(applicationContext);
    if (definition == null) {
      return;
    }
    if (applicationContext instanceof DefaultListableBeanFactory) {
      val beanFactory = (DefaultListableBeanFactory) applicationContext;
      beanFactory.setAutowireCandidateResolver(
        new ComponentAutowiredContextAnnotationAutowireCandidateResolver(definition));
    } else if (applicationContext instanceof GenericApplicationContext) {
      val beanFactory = ((GenericApplicationContext) applicationContext)
        .getDefaultListableBeanFactory();
      beanFactory.setAutowireCandidateResolver(
        new ComponentAutowiredContextAnnotationAutowireCandidateResolver(definition));
    }
    applicationContext
      .addApplicationListener(new ComponentBeanDetectApplicationListener(definition));
  }

  @RequiredArgsConstructor
  private static class ComponentAutowiredContextAnnotationAutowireCandidateResolver extends
    ContextAnnotationAutowireCandidateResolver {

    @NonNull
    private final SpringBootComponentDefinition definition;

    private void addComponentAutowired(DependencyDescriptor descriptor) {

      final Class<?> type = descriptor.getDependencyType();

      if (type.isArray()) {
        Class<?> componentType = type.getComponentType();
        ResolvableType resolvableType = descriptor.getResolvableType();
        Class<?> resolvedArrayType = resolvableType.resolve(type);
        if (resolvedArrayType != type) {
          componentType = resolvableType.getComponentType().resolve();
        }
        if (componentType != null) {
          definition.addConsumer(componentType);
        }
      } else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
        Class<?> elementType = descriptor.getResolvableType().asCollection().resolveGeneric();
        if (elementType != null) {
          definition.addConsumer(elementType);
        }

      } else {
        definition.addConsumer(type);
      }
    }

    @Override
    protected boolean isLazy(DependencyDescriptor descriptor) {
      boolean lazy = super.isLazy(descriptor);
      if (lazy) {
        for (val ann : descriptor.getAnnotations()) {
          val autowired = AnnotationUtils.getAnnotation(ann, ComponentAutowired.class);
          if (autowired != null && autowired.required()) {
            addComponentAutowired(descriptor);
            return true;
          }
        }
        val methodParam = descriptor.getMethodParameter();
        if (methodParam != null) {
          val method = methodParam.getMethod();
          if (method == null || void.class == method.getReturnType()) {
            val autowired = AnnotationUtils
              .getAnnotation(methodParam.getAnnotatedElement(), ComponentAutowired.class);
            if (autowired != null && autowired.required()) {
              addComponentAutowired(descriptor);
              return true;
            }
          }
        }
      }
      return lazy;
    }

  }

  @RequiredArgsConstructor
  private static class ComponentBeanDetectApplicationListener implements
    ApplicationListener<ApplicationStartedEvent> {

    private static final String ANNOTATION_NAME = ComponentBean.class.getName();

    @NonNull
    private final SpringBootComponentDefinition definition;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
      val applicationContext = event.getApplicationContext();
      val beanFactory = applicationContext.getBeanFactory();

      for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
        val beanDefinition = beanFactory
          .getBeanDefinition(beanDefinitionName);
        Map<String, Object> attributes = null;
        if (beanDefinition.getSource() instanceof MethodMetadata) {
          val metadata = (MethodMetadata) beanDefinition.getSource();
          if (metadata.isAnnotated(ANNOTATION_NAME)) {
            attributes = metadata.getAnnotationAttributes(ANNOTATION_NAME);
          }
        }
        if (beanDefinition instanceof AnnotatedBeanDefinition) {
          val metadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
          if (metadata.hasAnnotation(ANNOTATION_NAME)) {
            attributes = metadata.getAnnotationAttributes(ANNOTATION_NAME);
          }
        }

        if (attributes != null) {
          val host = Boolean.TRUE.equals(attributes.get("host"));
          Object bean = beanFactory.getBean(beanDefinitionName);
          Class<?> type = bean.getClass();
          if (!bean.getClass().isEnum()) {
            bean = ProxyFactory.getProxy(new SingletonTargetSource(bean));
          }
          definition.addBean(
            SpringBootComponentDefinition.ComponentBean.builder()
              .type(type)
              .host(host)
              .name(beanDefinitionName)
              .instance(bean)
              .build()
          );
        }
      }
      applicationContext.getBean(ApplicationEventMulticaster.class)
        .removeApplicationListener(this);
    }
  }

}
