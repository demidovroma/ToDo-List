package services

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import models._
import daos.TodoDAO

@Singleton
class TodoService @Inject()(
  todoDAO: TodoDAO
)(implicit ec: ExecutionContext) {

  // Получить все задачи
  def getAll: Future[Seq[Todo]] = {
    todoDAO.getAll
  }

  // Получить задачу по id
  def getTask(id: Int): Future[Option[Todo]] = {
    todoDAO.getById(id)
  }

  // Добавить новую задачу
  def addTask(title: String): Future[Todo] = {
    val TodoAddTask = models.TodoAddTask(title)
    todoDAO.addTask(TodoAddTask)
  }

  // Добавить новую задачу
  def updateTask(id: Int, newTitle: String): Future[Option[Todo]] = {
    val now = new java.sql.Timestamp(System.currentTimeMillis())
    val updateInfo = models.TodoUpdateTask(title = newTitle, updated = now)

    todoDAO.updateTask(id, updateInfo).flatMap { affectedRows =>
      if (affectedRows > 0) {
        todoDAO.getById(id)
      } else {
        Future.successful(None)
      }
    }
  }

  // Удалить задачу по id
  def deleteTask(id: Int): Future[Option[Todo]] = {
    val now = new java.sql.Timestamp(System.currentTimeMillis())
    todoDAO.getById(id).flatMap {
      case Some(todo) =>
        val deleteInfo = models.TodoDeleteTask(deleted = true, updated = now)

        todoDAO.deleteTaskById(id, deleteInfo).map { _ =>
          Some(todo.copy(deleted = true, updated = now))
        }
      case None =>
        Future.successful(None)
    }
  }

  // Переключение статуса "выполнена" задачи по id
  def completeTask(id: Int): Future[Option[Todo]] = {
    val now = new java.sql.Timestamp(System.currentTimeMillis())
    todoDAO.getById(id).flatMap {
      case Some(todo) =>
        val completeInfo = models.TodoCompleteTask(completed = true, updated = now)

        todoDAO.completeTaskById(id, completeInfo).map { _ =>
          Some(todo.copy(completed = true, updated = now))
        }
      case None =>
        Future.successful(None)
    }
  }

  // Переключение статуса "не выполнена" задачи по id
  def uncompleteTask(id: Int): Future[Option[Todo]] = {
    val now = new java.sql.Timestamp(System.currentTimeMillis())
    todoDAO.getById(id).flatMap {
      case Some(todo) =>
        val completeInfo = models.TodoCompleteTask(completed = false, updated = now)

        todoDAO.completeTaskById(id, completeInfo).map { _ =>
          Some(todo.copy(completed = false, updated = now))
        }
      case None =>
        Future.successful(None)
    }
  }

  // Переключение статуса "выполнена" всем задачам
  def completeAll(): Future[Int] = {
    val now = new java.sql.Timestamp(System.currentTimeMillis())
    todoDAO.completeAllTasks(desiredStatus = true, timestamp = now)
  }

  // Переключение статуса "не выполнена" всем задачам
  def uncompleteAll(): Future[Int] = {
    val now = new java.sql.Timestamp(System.currentTimeMillis())
    todoDAO.completeAllTasks(desiredStatus = false, timestamp = now)
  }

  // Удаление всех выполненых таск
  def deleteCompleted(): Future[Int] = {
    val now = new java.sql.Timestamp(System.currentTimeMillis())
    todoDAO.deleteCompleted(timestamp = now)
  }
}