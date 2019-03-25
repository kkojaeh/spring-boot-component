package kkojaeh.spring.boot.component;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class SpringBootComponentTestExecutionListener implements TestExecutionListener, Ordered {

  private final static String COMPONENT_TEST_INITIALIZED_PROPERTY = "spring.boot.component.test.initialized";

  public void beforeTestClass(TestContext testContext) throws Exception {
    val testComponent = testContext.getTestClass().getAnnotation(SpringBootTestComponent.class);

    if (testComponent != null) {
      val previous = testContext.getAttribute(COMPONENT_TEST_INITIALIZED_PROPERTY);
      val hashCode = testComponent.hashCode();

      if (previous != null) {
        if (previous.equals(hashCode)) {
          return;
        } else {
          testContext.markApplicationContextDirty(HierarchyMode.EXHAUSTIVE);
        }
      }
      testContext.setAttribute(COMPONENT_TEST_INITIALIZED_PROPERTY, hashCode);
      val parentContext = (ConfigurableApplicationContext) testContext.getApplicationContext();
      val builder = new SpringBootComponentBuilder(parentContext);
      for (val component : testComponent.classes()) {
        builder.component(component);
      }
      builder.run();
    }
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 1;
  }
}
