package kkojaeh.spring.boot.component;

import java.util.Set;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringBootComponentParentReadyEvent extends ApplicationEvent {

  private final Set<ConfigurableApplicationContext> components;

  public SpringBootComponentParentReadyEvent(ConfigurableApplicationContext parent,
    Set<ConfigurableApplicationContext> components) {
    super(parent);
    this.components = components;
  }

  public Set<ConfigurableApplicationContext> getComponents() {
    return components;
  }

  public ConfigurableApplicationContext getParent() {
    return (ConfigurableApplicationContext) this.getSource();
  }
}
