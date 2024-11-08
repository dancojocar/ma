package ro.cojocar.objectbox.domain

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class User(
  @Id
  var id: Long = 0,
  var name: String? = null,
  var email: String? = null
)