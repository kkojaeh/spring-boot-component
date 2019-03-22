package kkojaeh.test

import kkojaeh.parent.chainedtransaction.ChainedTransactionParentApplication
import kkojaeh.spring.boot.component.SpringBootComponentBuilder
import kkojaeh.todo.TodoModuleApplication
import kkojaeh.todo.TodoService
import kkojaeh.user.UserModuleApplication
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Lazy
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@RunWith(SpringRunner::class)
@ComponentScan(useDefaultFilters = false)
@SpringBootTest(classes = [ChainedTransactionParentApplication::class])
@Transactional
@Rollback
@ActiveProfiles("test")
class ChainedTransactionApplicationTests() {

  @Autowired
  lateinit var parent: ConfigurableApplicationContext

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
