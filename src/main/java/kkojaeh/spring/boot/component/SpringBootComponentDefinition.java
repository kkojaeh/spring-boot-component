package kkojaeh.spring.boot.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

  private static final String MODULE_DEFINITION_PROPERTY = "spring.module.definition";

  @NonNull
  @Getter
  private final String name;

  private final Set<Class<?>> takes = new HashSet<>();

  private final Set<Class<?>> gives = new HashSet<>();

  public static SpringBootComponentDefinition from(ConfigurableApplicationContext context) {
    return context.getEnvironment()
      .getProperty(MODULE_DEFINITION_PROPERTY, SpringBootComponentDefinition.class);
  }

  public static List<SpringBootComponentDefinition> sort(
    Collection<SpringBootComponentDefinition> definitions) {
    val result = new LinkedList<SpringBootComponentDefinition>();
    val targets = new LinkedList<SpringBootComponentDefinition>(definitions);

    val gives = new HashMap<Class<?>, Set<SpringBootComponentDefinition>>();
    val takes = new HashSet<Class<?>>();
    val dependencies = new HashMap<SpringBootComponentDefinition, Set<SpringBootComponentDefinition>>();

    definitions.forEach(definition -> {
      dependencies.put(definition, new HashSet<>());
      takes.addAll(definition.takes);
      definition.gives.forEach(give -> {
        if (!gives.containsKey(give)) {
          gives.put(give, new HashSet<>());
        }
        gives.get(give).add(definition);
      });

    });

    gives.forEach((give, set) -> {
      takes.removeAll(
        takes.stream().filter(take -> take.isAssignableFrom(give))
          .collect(Collectors.toSet())
      );
      definitions.forEach(definition -> {
        val dependOn = definition.takes.stream().anyMatch(take -> take.isAssignableFrom(give));
        if (dependOn) {
          dependencies.get(definition).addAll(set);
        }
      });
    });

    if (!takes.isEmpty()) {
      throw new RuntimeException("cannot find giver for: " +
        takes.stream()
          .map(take -> take.getName())
          .collect(Collectors.joining(", "))
      );
    }

    outer:
    while (!targets.isEmpty()) {
      for (SpringBootComponentDefinition target : targets) {
        val hasDependency = dependencies.get(target)
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

  public void addGive(Class<?> give) {
    gives.add(give);
  }

  public void addTake(Class<?> take) {
    takes.add(take);
  }

  public void to(ConfigurableEnvironment environment) {
    Map<String, Object> map = new HashMap<>();
    map.put(ConfigFileApplicationListener.CONFIG_LOCATION_PROPERTY,
      String.format("classpath:/%s/", name));
    map.put(MODULE_DEFINITION_PROPERTY, this);
    environment.getPropertySources().addFirst(
      new MapPropertySource("module-property-source", map)
    );

  }

}
