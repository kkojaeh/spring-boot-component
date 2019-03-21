package kkojaeh.test

import kkojaeh.parent.onedatasource.OneDataSourceParentApplication
import kkojaeh.spring.boot.component.SpringBootComponentBuilder
import kkojaeh.todo.TodoModuleApplication
import kkojaeh.user.UserModuleApplication

class Test

fun main(args: Array<String>) {
  SpringBootComponentBuilder(OneDataSourceParentApplication::class.java)
    .add(TodoModuleApplication::class.java)
    .add(UserModuleApplication::class.java)
    .run(*args)
}
