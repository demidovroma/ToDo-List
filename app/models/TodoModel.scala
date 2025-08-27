package models

import java.sql.Timestamp
import play.api.libs.json._
import utils.JsonImplicits._

// Модель данных таблицы
case class Todo(
  id: Int,
  title: String,
  completed: Boolean,
  created: Timestamp,
  updated: Timestamp,
  deleted: Boolean
)

object Todo {
  implicit val format: OFormat[Todo] = Json.format[Todo]
}

// Модель добавления новой задачи
case class TodoAddTask(title: String)
object TodoAddTask {
  implicit val format: OFormat[TodoAddTask] = Json.format[TodoAddTask]
}

// Модель изменения задачи
case class TodoUpdateTask(title: String)
object TodoUpdateTask {
  implicit val format: OFormat[TodoUpdateTask] = Json.format[TodoUpdateTask]
}