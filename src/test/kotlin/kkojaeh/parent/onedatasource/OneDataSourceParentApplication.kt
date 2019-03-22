package kkojaeh.parent.onedatasource

import kkojaeh.spring.boot.component.SpringBootComponent
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration

@SpringBootComponent("parent/one-data-source")
@SpringBootApplication(exclude = [
  //DataSourceAutoConfiguration::class,
  DataSourceTransactionManagerAutoConfiguration::class,
  JpaRepositoriesAutoConfiguration::class,
  HibernateJpaAutoConfiguration::class
])
class OneDataSourceParentApplication

fun main(args: Array<String>) {
  SpringApplication.run(OneDataSourceParentApplication::class, *args)
}
