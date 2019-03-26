package kkojaeh.test

import kkojaeh.parent.onedatasource.OneDataSourceParentApplication
import kkojaeh.spring.boot.component.SpringBootTestComponent
import kkojaeh.spring.boot.component.Take
import kkojaeh.todo.TodoModuleApplication
import kkojaeh.todo.TodoService
import kkojaeh.user.UserModuleApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ComponentScan(useDefaultFilters = false)
@SpringBootTest(classes = [TodoModuleApplication::class])
@SpringBootTestComponent(parent = OneDataSourceParentApplication::class, siblings = [UserModuleApplication::class])
class OneDataSourceApplicationTests {

  @Take
  lateinit var todoService: TodoService

  @Test
  fun aa() {
    println(todoService.todos())
  }

}
