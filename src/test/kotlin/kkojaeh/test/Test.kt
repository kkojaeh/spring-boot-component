package kkojaeh.test

import kkojaeh.spring.boot.component.SpringBootComponentBuilder
import kkojaeh.user.UserModuleApplication

class Test

fun main(args: Array<String>) {
  SpringBootComponentBuilder()
    .component(UserModuleApplication::class.java)
    .run(*args)
}
