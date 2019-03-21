package kkojaeh.spring.boot.component;

import java.util.Set;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringBootComponentParentReadyEvent extends ApplicationEvent {

  private final Set<ConfigurableApplicationContext> units;

  public SpringBootComponentParentReadyEvent(ConfigurableApplicationContext parent,
    Set<ConfigurableApplicationContext> modules) {
    super(parent);
    this.units = modules;
  }

  public Set<ConfigurableApplicationContext> getUnits() {
    return units;
  }

  public ConfigurableApplicationContext getParent() {
    return (ConfigurableApplicationContext) this.getSource();
  }
}
