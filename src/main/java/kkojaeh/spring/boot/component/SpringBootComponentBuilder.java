package kkojaeh.spring.boot.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

public class SpringBootComponentBuilder {

  public SpringBootComponentBuilder() {
    this(NoOpParent.class);
  }

  private final List<Class<?>> components = new LinkedList<>();

  @Getter
  private final Set<ConfigurableApplicationContext> componentContexts = new HashSet<>();

  @Getter
  private final Map<Class<?>, Consumer<SpringApplicationBuilder>> consumers = new HashMap<>();

  private Class<?> parent;

  @Getter
  private ConfigurableApplicationContext parentContext;

  private boolean parallel = true;

  private int parallelSize = 10;

  public SpringBootComponentBuilder component(@NonNull Class<?> source) {
    return component(source, (builder) -> {
    });
  }

  public SpringBootComponentBuilder component(@NonNull Class<?> source,
    @NonNull Consumer<SpringApplicationBuilder> consumer) {
    if (components.contains(source)) {
      throw new RuntimeException(String.format("%s is already exists", source.getName()));
    }
    components.add(source);
    consumers.put(source, consumer);
    return this;
  }

  SpringBootComponentBuilder component(@NonNull ConfigurableApplicationContext context) {
    componentContexts.add(context);
    return this;
  }

  public SpringBootComponentBuilder(Class<?> parent) {
    this.parent = parent;
  }

  public SpringBootComponentBuilder(ConfigurableApplicationContext parent) {
    this.parentContext = parent;
  }

  public SpringBootComponentBuilder parallel(boolean parallel) {
    this.parallel = parallel;
    return this;
  }

  public SpringBootComponentBuilder parallelSize(int parallelSize) {
    this.parallelSize = parallelSize;
    return this;
  }

  @Configuration
  public static class NoOpParent {

  }

  @SneakyThrows
  public Map<Class<?>, ConfigurableApplicationContext> run(String... args) {
    if (parentContext == null) {
      parentContext = SpringApplication.run(parent, args);
    }

    componentContexts.forEach(componentContext -> componentContext.setParent(parentContext));

    val contexts = new HashMap<Class<?>, ConfigurableApplicationContext>();
    if (parallel) {
      ForkJoinPool forkjoinPool = new ForkJoinPool(parallelSize);
      forkjoinPool.submit(() -> {
        components.parallelStream()
          .forEach(component -> {
            val builder = new SpringApplicationBuilder(component)
              .parent(parentContext);
            if (consumers.containsKey(component)) {
              consumers.get(component).accept(builder);
            }
            contexts.put(component, builder.run(args));
          });
      }).get();
    } else {
      components.forEach(component -> {
        val builder = new SpringApplicationBuilder(component)
          .parent(parentContext);
        if (consumers.containsKey(component)) {
          consumers.get(component).accept(builder);
        }
        contexts.put(component, builder.run(args));
      });
    }
    componentContexts.addAll(contexts.values());
    val componentSet = new HashSet<ConfigurableApplicationContext>(componentContexts);
    val definitions = new HashMap<SpringBootComponentDefinition, ConfigurableApplicationContext>();
    componentSet.forEach(context -> {
      val definition = SpringBootComponentDefinition.from(context);
      definitions.put(definition, context);
    });

    definitions.forEach((definition, context) -> {
      val siblings = componentSet.stream()
        .filter(c -> !c.equals(context))
        .collect(Collectors.toSet());

      siblings.forEach(c -> {
        val beanFactory = c.getBeanFactory();
        definition.getComponentBeans().forEach(bean -> {
          beanFactory.registerSingleton(
            String.format("%s/%s", definition.getName(), bean.getName()),
            bean.getInstance()
          );
        });
      });

    });

    val parentEvent = new SpringBootComponentParentReadyEvent(parentContext,
      Collections.unmodifiableSet(componentSet));
    parentContext.publishEvent(parentEvent);

    val sortedUnits = SpringBootComponentDefinition.sort(definitions.keySet());
    sortedUnits.forEach(definition -> {
      val context = definitions.get(definition);
      context.publishEvent(new SpringBootComponentReadyEvent(context));
    });
    return contexts;
  }

}
