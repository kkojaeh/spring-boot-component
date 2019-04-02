package kkojaeh.parent.onedatasource

import kkojaeh.spring.boot.component.SpringBootComponent
import kkojaeh.spring.boot.component.SpringBootComponentParentReadyEvent
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import javax.sql.DataSource

@SpringBootComponent("parent/one-data-source")
@SpringBootApplication(exclude = [
  //DataSourceAutoConfiguration::class,
  DataSourceTransactionManagerAutoConfiguration::class,
  JpaRepositoriesAutoConfiguration::class,
  HibernateJpaAutoConfiguration::class
])
class OneDataSourceParentApplication : ApplicationListener<SpringBootComponentParentReadyEvent> {
  override fun onApplicationEvent(event: SpringBootComponentParentReadyEvent) {
    val dataSource = event.parent.getBean(DataSource::class.java)
    val builder = BeanDefinitionBuilder.genericBeanDefinition(DataSource::class.java) {
      DelegateDataSource(dataSource)
    }
    val beanDefinition = builder.beanDefinition
    beanDefinition.isPrimary = true
    event.components.forEach { component -> (component as BeanDefinitionRegistry).registerBeanDefinition("delegateDataSource", beanDefinition) }
  }

  class DelegateDataSource(source: DataSource) : DataSource by source

}

fun main(args: Array<String>) {
  runApplication<OneDataSourceParentApplication>(*args)
}
