package kkojaeh.todo

import kkojaeh.spring.boot.component.SpringBootComponent
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@EnableJpaRepositories
@EntityScan
@EnableAspectJAutoProxy
@EnableTransactionManagement
@SpringBootComponent("todo")
@SpringBootApplication
class TodoModuleApplication{

  @Primary
  @Bean
  @ConditionalOnBean(DataSource::class)
  fun delegateDataSource(dataSource: DataSource): DelegateDataSource {
    return DelegateDataSource(dataSource)
  }

  class DelegateDataSource(source: DataSource) : DataSource by source
}

fun main(args: Array<String>) {
  SpringApplication.run(TodoModuleApplication::class, *args)
}
