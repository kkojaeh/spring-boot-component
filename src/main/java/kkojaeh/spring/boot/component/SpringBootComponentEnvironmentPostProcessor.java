package kkojaeh.spring.boot.component;

import java.io.IOException;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

public class SpringBootComponentEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  public static final String MODULE_NAME_PROPERTY = "spring.module.name";

  @Override
  public int getOrder() {
    return ConfigFileApplicationListener.DEFAULT_ORDER - 1;
  }

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
    SpringApplication application) {

    SimpleMetadataReaderFactory factory = new SimpleMetadataReaderFactory();

    String bootModuleName = null;

    for (Object source : application.getAllSources()) {
      if (!(source instanceof Class)) {
        continue;
      }
      Class sourceClass = (Class) source;
      String className = sourceClass.getName();

      MetadataReader reader = null;
      try {
        reader = factory.getMetadataReader(className);
        boolean annotated = reader.getAnnotationMetadata()
          .hasAnnotation(SpringBootComponent.class.getName());
        if (annotated) {
          Map<String, Object> values = reader.getAnnotationMetadata().getAnnotationAttributes(
            SpringBootComponent.class.getName());

          Object value = values.get("value");
          if (value != null) {
            if (bootModuleName != null) {
              throw new RuntimeException(String
                .format("boot module name duplicated: '%s', '%s'", bootModuleName,
                  value.toString()));
            } else {
              bootModuleName = value.toString();
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }

    if (bootModuleName != null) {
      new SpringBootComponentDefinition(bootModuleName).to(environment);
    }
  }
}
