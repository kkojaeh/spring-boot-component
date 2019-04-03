package kkojaeh.test

import kkojaeh.parent.chainedtransaction.ChainedTransactionParentApplication
import kkojaeh.spring.boot.component.ComponentAutowired
import kkojaeh.spring.boot.component.SpringBootTestComponent
import kkojaeh.todo.TodoModuleApplication
import kkojaeh.todo.TodoService
import kkojaeh.user.UserModuleApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@ComponentScan(useDefaultFilters = false)
@SpringBootTest(classes = [TodoModuleApplication::class])
@SpringBootTestComponent(parent = ChainedTransactionParentApplication::class, siblings = [UserModuleApplication::class])
@Transactional
@Rollback
@ActiveProfiles("test")
class ChainedTransactionApplicationTests {

  @ComponentAutowired
  lateinit var todoService: TodoService

  @Test
  fun aa() {
    println(todoService.todos())
  }

}
