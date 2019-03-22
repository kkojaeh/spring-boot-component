package kkojaeh.test

import kkojaeh.parent.onedatasource.OneDataSourceParentApplication
import kkojaeh.spring.boot.component.SpringBootComponentBuilder
import kkojaeh.todo.TodoModuleApplication
import kkojaeh.todo.TodoService
import kkojaeh.user.UserModuleApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Lazy
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.annotation.PostConstruct

@ExtendWith(SpringExtension::class)
@ComponentScan(useDefaultFilters = false)
@SpringBootTest(classes = [OneDataSourceParentApplication::class])
class OneDataSourceApplicationTests(val parent: ConfigurableApplicationContext) {

  @PostConstruct
  fun init() {
    SpringBootComponentBuilder(parent)
      .component(TodoModuleApplication::class.java)
      .component(UserModuleApplication::class.java)
      .run()
  }

  @Lazy
  @Autowired
  lateinit var todoService: TodoService

  @Test
  fun aa() {
    println(todoService.todos())
  }

}
