package models.dto

import models.db.Todo

case class TodoResponse(
  id: Int,
  title: String,
  completed: Boolean
)

object TodoResponse {
  def fromDb(todo: Todo): TodoResponse =
    TodoResponse(todo.id, todo.title, todo.completed)
}
