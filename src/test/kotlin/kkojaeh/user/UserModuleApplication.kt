package kkojaeh.user

import kkojaeh.spring.boot.component.SpringBootComponent
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
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
@SpringBootComponent("user")
@SpringBootApplication
class UserModuleApplication{

  @Primary
  @Bean
  @ConditionalOnBean(DataSource::class)
  fun delegateDataSource(dataSource: DataSource): DataSource{
    return DelegateDataSource(dataSource)
  }

  class DelegateDataSource(source: DataSource) : DataSource by source
}
fun main(args: Array<String>) {
  runApplication<UserModuleApplication>(*args)
}
