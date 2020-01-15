package ro.ubbcluj.cs.books.db

import androidx.room.TypeConverter
import java.util.*

class Converters {
  @TypeConverter
  fun fromTimestamp(value: Long?): Date? {
    return if (value == null) null else Date(value)
  }

  @TypeConverter
  fun dateToTimestamp(date: Date?): Long? {
    return date?.time
  }
}
