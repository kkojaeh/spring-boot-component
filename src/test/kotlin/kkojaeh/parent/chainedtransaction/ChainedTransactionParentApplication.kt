package kkojaeh.parent.chainedtransaction

import kkojaeh.spring.boot.component.SpringBootComponent
import kkojaeh.spring.boot.component.SpringBootComponentParentReadyEvent
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.transaction.ChainedTransactionManager
import org.springframework.transaction.PlatformTransactionManager

@SpringBootComponent("parent/chained-transaction")
@SpringBootApplication(exclude = [
  DataSourceAutoConfiguration::class,
  JpaRepositoriesAutoConfiguration::class,
  HibernateJpaAutoConfiguration::class,
  DataSourceTransactionManagerAutoConfiguration::class])
class ChainedTransactionParentApplication : ApplicationListener<SpringBootComponentParentReadyEvent> {

  override fun onApplicationEvent(event: SpringBootComponentParentReadyEvent) {
    fun toPlatformTransactionManager(component: ConfigurableApplicationContext): PlatformTransactionManager? {
      if (component.getBeanNamesForType(PlatformTransactionManager::class.java).isNotEmpty()) {
        return component.getBean(PlatformTransactionManager::class.java)
      }
      return null
    }

    val transactionManagers = event.components.map { component ->
      toPlatformTransactionManager(component)
    }.filter { it != null }.toTypedArray()

    val builder = BeanDefinitionBuilder.genericBeanDefinition(PlatformTransactionManager::class.java) {
      ChainedTransactionManager(*transactionManagers)
    }

    val beanDefinition = builder.beanDefinition
    beanDefinition.isPrimary = true
    (event.parent as BeanDefinitionRegistry).registerBeanDefinition("transactionManager", beanDefinition)
  }

}

fun main(args: Array<String>) {
  runApplication<ChainedTransactionParentApplication>(*args)
}
