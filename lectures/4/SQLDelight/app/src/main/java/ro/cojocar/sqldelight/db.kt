package ro.cojocar.sqldelight

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver

fun setupDatabase(context: Context): PlayerQueries {
  val androidSqlDriver = AndroidSqliteDriver(
    schema = Database.Schema,
    context = context,
    name = "players.db"
  )
  return Database(androidSqlDriver).playerQueries
}