package models.db

case class Todo(
  id: Int,
  title: String,
  completed: Boolean,
  deleted: Boolean
)
