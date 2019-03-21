package kkojaeh.user

import kkojaeh.spring.boot.component.Give
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.sql.DataSource

@Give
@Service
@Transactional
class UserService(
  val dataSource: DataSource,
  @Value("\${user.name}") val userName: String) {

  fun name(): String {
    return userName
  }

}



