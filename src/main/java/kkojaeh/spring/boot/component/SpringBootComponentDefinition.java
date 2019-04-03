package kkojaeh.spring.boot.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

@EqualsAndHashCode(of = "name")
@RequiredArgsConstructor
public class SpringBootComponentDefinition {

  private static final String COMPONENT_DEFINITION_PROPERTY = "spring.component.definition";

  @NonNull
  @Getter
  private final String name;

  private final Set<Class<?>> consumers = new HashSet<>();

  @Getter
  private final Set<ComponentBean> componentBeans = new HashSet<>();

  public static SpringBootComponentDefinition from(ConfigurableApplicationContext context) {
    return context.getEnvironment()
      .getProperty(COMPONENT_DEFINITION_PROPERTY, SpringBootComponentDefinition.class);
  }

  public static List<SpringBootComponentDefinition> sort(
    Collection<SpringBootComponentDefinition> definitions) {
    val result = new LinkedList<SpringBootComponentDefinition>();
    val targets = new LinkedList<SpringBootComponentDefinition>(definitions);

    val providers = new HashMap<Class<?>, Set<SpringBootComponentDefinition>>();
    val consumers = new HashSet<Class<?>>();
    val dependencies = new HashMap<SpringBootComponentDefinition, Set<SpringBootComponentDefinition>>();

    definitions.forEach(definition -> {
      dependencies.put(definition, new HashSet<>());
      consumers.addAll(definition.consumers);
      definition.componentBeans.forEach(bean -> {
        val type = bean.getType();
        if (!providers.containsKey(type)) {
          providers.put(type, new HashSet<>());
        }
        if (bean.isHost()) {
          providers.get(type).add(definition);
        }
      });

    });

    providers.forEach((provider, set) -> {
      consumers.removeAll(
        consumers.stream().filter(consumer -> consumer.isAssignableFrom(provider))
          .collect(Collectors.toSet())
      );
      definitions.forEach(definition -> {
        val dependOn = definition.consumers.stream()
          .anyMatch(autowired -> autowired.isAssignableFrom(provider));
        if (dependOn) {
          dependencies.get(definition).addAll(set);
        }
      });
    });

    if (!consumers.isEmpty()) {
      throw new RuntimeException("cannot find bean for: " +
        consumers.stream()
          .map(consumer -> consumer.getName())
          .collect(Collectors.joining(", "))
      );
    }

    outer:
    while (!targets.isEmpty()) {
      for (SpringBootComponentDefinition target : targets) {
        val hasDependency = dependencies.get(target.getName())
          .stream()
          .anyMatch(dependency -> targets.stream().anyMatch(t -> t.equals(dependency)));
        if (!hasDependency) {
          targets.remove(target);
          result.add(target);
          continue outer;
        }
      }
      throw new RuntimeException("Graph has cycles");
    }
    return result;
  }

  public void addBean(ComponentBean bean) {
    componentBeans.add(bean);
  }

  public void addConsumer(Class<?> consumer) {
    consumers.add(consumer);
  }

  public void to(ConfigurableEnvironment environment) {
    Map<String, Object> map = new HashMap<>();
    map.put(ConfigFileApplicationListener.CONFIG_LOCATION_PROPERTY,
      String.format("classpath:/%s/", name));
    map.put(COMPONENT_DEFINITION_PROPERTY, this);
    environment.getPropertySources().addFirst(
      new MapPropertySource("component-property-source", map)
    );

  }

  @Builder
  @Getter
  public static class ComponentBean {

    Class<?> type;

    boolean host;

    String name;

    Object instance;

  }

}
