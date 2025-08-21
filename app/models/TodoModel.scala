package models

import java.sql.Timestamp
import play.api.libs.json._

// Модель данных таблицы
case class Todo(
  id: Int,
  title: String,
  completed: Boolean,
  created: java.sql.Timestamp,
  updated: java.sql.Timestamp,
  deleted: Boolean
)

// Модель добавления новой таски
case class TodoAddTask(
  title: String
)

// Модель изменения таски
case class TodoUpdateTask(
  title: String,
  updated: java.sql.Timestamp
)

// Модель удаления таски
case class TodoDeleteTask(
  deleted: Boolean,
  updated: java.sql.Timestamp
)

// Модель отметки выполнения таски
case class TodoCompleteTask(
  completed: Boolean,
  updated: java.sql.Timestamp
)

object Todo {
  // Имплиситный формат для Timestamp
  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    def writes(ts: Timestamp): JsValue = JsString(ts.toInstant.toString)
    def reads(json: JsValue): JsResult[Timestamp] = json match {
      case JsString(str) =>
        try {
          val instant = java.time.Instant.parse(str)
          JsSuccess(Timestamp.from(instant))
        } catch {
          case e: Exception => JsError(e.getMessage)
        }
      case _ => JsError("Expected string for Timestamp")
    }
  }

  implicit val todoFormat: Format[Todo] = Json.format[Todo]
}