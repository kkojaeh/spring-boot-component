package kkojaeh.spring.boot.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
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

  private Class<?> parent;

  private ConfigurableApplicationContext parentContext;

  private boolean parallel = true;

  private int parallelSize = 10;

  public SpringBootComponentBuilder component(Class<?> source) {
    if (components.contains(source)) {
      throw new RuntimeException(String.format("%s is already exists", source.getName()));
    }
    components.add(source);
    return this;
  }

  public SpringBootComponentBuilder(Class<?> parent) {
    this.parent = parent;
  }

  public SpringBootComponentBuilder(ConfigurableApplicationContext parent) {
    this.parentContext = parent;
  }

  public SpringBootComponentBuilder parallel(boolean parallel){
    this.parallel = parallel;
    return this;
  }

  public SpringBootComponentBuilder parallelSize(int parallelSize){
    this.parallelSize = parallelSize;
    return this;
  }

  @Configuration
  public static class NoOpParent {

  }

  @SneakyThrows
  public Map<Class<?>, ConfigurableApplicationContext> run(String... args) {
    long start = System.currentTimeMillis();
    if (parentContext == null) {
      parentContext = SpringApplication.run(parent, args);
    }

    val contexts = new HashMap<Class<?>, ConfigurableApplicationContext>();
    if (parallel) {
      ForkJoinPool forkjoinPool = new ForkJoinPool(parallelSize);
      forkjoinPool.submit(() -> {
        components.parallelStream()
          .forEach(component ->
            contexts.put(component,
              new SpringApplicationBuilder(component)
                .parent(parentContext)
                .run(args))
          );
      }).get();
    } else {
      components.forEach(component ->
        contexts.put(component,
          new SpringApplicationBuilder(component)
            .parent(parentContext)
            .run(args))
      );
    }

    val componentSet = new HashSet<ConfigurableApplicationContext>(contexts.values());
    val parentEvent = new SpringBootComponentParentReadyEvent(parentContext,
      Collections.unmodifiableSet(componentSet));
    parentContext.publishEvent(parentEvent);
    val definitions = new HashMap<SpringBootComponentDefinition, ConfigurableApplicationContext>();
    componentSet.forEach(context -> {
      definitions.put(SpringBootComponentDefinition.from(context), context);
    });

    val sortedUnits = SpringBootComponentDefinition.sort(definitions.keySet());
    sortedUnits.forEach(definition -> {
      val context = definitions.get(definition);
      context.publishEvent(new SpringBootComponentReadyEvent(context));
    });
    return contexts;
  }

}
