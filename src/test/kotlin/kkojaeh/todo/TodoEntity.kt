package kkojaeh.todo

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class TodoEntity(@Id
                      var id: String,
                      var name: String)
