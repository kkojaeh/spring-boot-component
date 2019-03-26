package kkojaeh.spring.boot.component;

import java.util.Map;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class SpringBootComponentTestExecutionListener implements TestExecutionListener, Ordered {

  private Map<Class<?>, ConfigurableApplicationContext> components;

  private ConfigurableApplicationContext parent;

  @Override
  public void afterTestClass(TestContext testContext) throws Exception {
    if (components != null) {
      val mainContext = (ConfigurableApplicationContext) testContext.getApplicationContext();
      components.forEach((type, context) -> {
        //if (!context.equals(mainContext)) {
          context.stop();
          context.close();
        //}
      });
    }
    if (parent != null) {
      parent.stop();
      parent.close();
    }

  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 1;
  }

  public void beforeTestClass(TestContext testContext) throws Exception {

    val testComponent = testContext.getTestClass().getAnnotation(SpringBootTestComponent.class);
    if (testComponent != null) {
      val mainContext = (ConfigurableApplicationContext) testContext.getApplicationContext();
      val builder = new SpringBootComponentBuilder(testComponent.parent());
      builder.component(mainContext);
      for (val component : testComponent.siblings()) {
        builder.component(component);
      }
      components = builder.run();
      parent = builder.getParentContext();
    }
  }
}
