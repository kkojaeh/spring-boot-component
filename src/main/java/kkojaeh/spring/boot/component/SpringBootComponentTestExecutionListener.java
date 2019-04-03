package kkojaeh.spring.boot.component;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

public class SpringBootComponentTestExecutionListener implements TestExecutionListener, Ordered {

  private static ThreadLocal<SpringBootTestComponent> currentComponent = new ThreadLocal<>();

  private static ThreadLocal<SpringBootComponentBuilder> currentBuilder = new ThreadLocal<>();

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 1;
  }

  public void beforeTestClass(TestContext testContext) throws Exception {
    val testComponent = testContext.getTestClass().getAnnotation(SpringBootTestComponent.class);
    if (testComponent != null) {
      val previous = currentComponent.get();
      val builder = currentBuilder.get();
      if (previous != null) {
        if (previous.equals(testComponent)) {
          return;
        } else if (builder != null) {
          testContext.markApplicationContextDirty(HierarchyMode.CURRENT_LEVEL);
          testContext
            .setAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE,
              Boolean.TRUE);

          builder.getComponentContexts().forEach(context -> {
            context.stop();
            context.close();
          });
          builder.getParentContext().stop();
          builder.getParentContext().close();
        }
      }
      currentComponent.set(testComponent);
      val mainContext = (ConfigurableApplicationContext) testContext.getApplicationContext();
      val newBuilder = new SpringBootComponentBuilder(testComponent.parent());
      mainContext.setParent(null);
      newBuilder.component(mainContext);
      for (val component : testComponent.siblings()) {
        newBuilder.component(component);
      }
      newBuilder.run();
      currentBuilder.set(newBuilder);
    }
  }
}
