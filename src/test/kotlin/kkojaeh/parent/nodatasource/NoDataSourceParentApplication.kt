package kkojaeh.parent.nodatasource

import kkojaeh.spring.boot.component.SpringBootComponent
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootComponent("parent/no-data-source")
@SpringBootApplication(exclude = [
  DataSourceAutoConfiguration::class,
  JpaRepositoriesAutoConfiguration::class,
  HibernateJpaAutoConfiguration::class,
  DataSourceTransactionManagerAutoConfiguration::class])
class NoDataSourceParentApplication

fun main(args: Array<String>) {
  runApplication<NoDataSourceParentApplication>(*args)
}
