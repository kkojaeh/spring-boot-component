package kkojaeh.todo

import kkojaeh.spring.boot.component.Give
import kkojaeh.spring.boot.component.Take
import kkojaeh.user.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Give
@Service
@Transactional
class TodoService(
  @Take val userService: UserService,
  @PersistenceContext val entityManager: EntityManager,
  @Value("\${todo}") val todo: String) {

  fun todos(): String {
    val todo = TodoEntity(id = "a", name = "에이")
    entityManager.persist(todo)
    return userService.name() + " : " + todo
  }

}



