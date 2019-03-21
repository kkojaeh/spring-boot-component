package kkojaeh.spring.boot.component;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringBootComponentReadyEvent extends ApplicationEvent {

  public SpringBootComponentReadyEvent(ConfigurableApplicationContext context) {
    super(context);
  }

}
