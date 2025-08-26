package services

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import java.sql.{Connection, Timestamp}

import scala.util.control.Exception.allCatch

import play.api.Logger
import io.sentry.{Sentry, SentryOptions}
import models._
import daos.TodoDAO

@Singleton
class TodoService @Inject()(
  todoDAO: TodoDAO
)(implicit ec: ExecutionContext) {

  val now = new java.sql.Timestamp(System.currentTimeMillis())
  // Константы для валидации
  private val maxTitleLength = 255
  private val allowedCharsRegex = """^a-zA-Z0-9\s.,!?@#$%^&*()_+=-+$""".r

  private val logger = Logger(this.getClass)

  // Обработка ошибок
  private def handleError[T](ex: Throwable, message: String): Future[T] = {
    logger.error(s"$message: ${ex.getMessage}", ex)
    Sentry.captureException(ex)
    Future.failed(ex)
  }

  // Проверка ID
  private def validateId(id: Int): Unit = {
    if (id <= 0) {
      handleError[Unit]( new IllegalArgumentException("ID задачи должен быть положительным числом"), "Ошибка валидации ID")
    } else {
      Future.successful(())
    }
  }

  // Получить все задачи
  def getAll: Future[Seq[Todo]] = {
    todoDAO.getAll.recoverWith {
      case ex: Exception =>
        handleError[Seq[Todo]](ex, "Ошибка при получении всех задач")
    }
  }

  // Получить задачу по id
  def getTask(id: Int): Future[Option[Todo]] = {
    validateId(id)

    todoDAO.getById(id).recoverWith {
      case ex: Exception =>
        handleError[Option[Todo]](ex, s"Ошибка при получении задачи с id: $id")
    }
  }

  // Добавить новую задачу
  def addTask(title: String): Future[Todo] = {
    val maxTitleLength = 255
    val allowedCharsRegex = """^[a-zA-Z0-9\s.,!?@#$%^&*()_+=-]+$""".r

    if (title.trim.isEmpty) {
      handleError[Todo](new IllegalArgumentException("Заголовок задачи не может быть пустым"), "Ошибка добавления задачи")
    } else if (title.length > maxTitleLength) {
      handleError[Todo](new IllegalArgumentException("Превышена максимальная длина заголовка (максимум 255 символов)"), "Ошибка валидации заголовка")
    } else if (!allowedCharsRegex.matches(title)) {
      handleError[Todo](new IllegalArgumentException("Недопустимые символы в заголовке"), "Ошибка валидации заголовка")
    } else {

      val todoAddTask = models.TodoAddTask(title)
      todoDAO.addTask(todoAddTask).recoverWith {
        case ex: Exception =>
          handleError[Todo](ex, "Ошибка при добавлении задачи")
      }
    }
  }

  // Добавить новую задачу
  def updateTask(id: Int, newTitle: String): Future[Option[Todo]] = {
    validateId(id)

    if (newTitle.trim.isEmpty) {
      handleError[Option[Todo]](new IllegalArgumentException("Новый заголовок задачи не может быть пустым"), "Ошибка обновления задачи")
    } else if (newTitle.length > maxTitleLength) {
      handleError[Option[Todo]](new IllegalArgumentException("Превышена максимальная длина заголовка (максимум 255 символов)"), "Ошибка валидации заголовка")
    } else if (!allowedCharsRegex.matches(newTitle)) {
      handleError[Option[Todo]](new IllegalArgumentException("Недопустимые символы в заголовке"), "Ошибка валидации заголовка")
    } else {
      val now = new Timestamp(System.currentTimeMillis())
      val updateInfo = TodoUpdateTask(title = newTitle, updated = now)

      todoDAO.updateTask(id, updateInfo).flatMap { affectedRows =>
        if (affectedRows > 0) {
          todoDAO.getById(id)
        } else {
          Future.successful(None)
        }
      }.recoverWith {
        case ex: Exception =>
          handleError[Option[Todo]](ex, s"Ошибка при обновлении задачи с id: $id")
      }
    }
  }

  // Удалить задачу по id
  def deleteTask(id: Int): Future[Option[Todo]] = {
    validateId(id)

    todoDAO.getById(id).flatMap {
      case Some(todo) =>
        if (todo.deleted) {
          Future.failed(new IllegalStateException("Задача уже помечена как удаленная")).asInstanceOf[Future[Option[Todo]]]
        }
        val deleteInfo = models.TodoDeleteTask(deleted = true, updated = now)

        todoDAO.deleteTaskById(id, deleteInfo).map { affectedRows =>
          if (affectedRows == 0) {
            Future.failed(new Exception(s"Не удалось удалить задачу с ID: $id")).asInstanceOf[Future[Option[Todo]]]
          }
          Some(todo.copy(deleted = true, updated = now))
        }.recoverWith {
          case ex: Exception =>
            Future.failed(ex)
        }

      case None =>
        Future.successful(None)
    }.recoverWith {
      case ex: Exception =>
        Future.failed(ex)
    }
  }

  // Переключение статуса "выполнена" задачи по id
  def completeTask(id: Int): Future[Option[Todo]] = {
    validateId(id)

    todoDAO.getById(id).flatMap {
      case Some(todo) =>
        val completeInfo = models.TodoCompleteTask(completed = true, updated = now)

        todoDAO.completeTaskById(id, completeInfo).map { affectedRows =>
          if (affectedRows == 0) {
            Future.failed(new Exception(s"Не удалось отметить выполненой задачу с ID: $id")).asInstanceOf[Future[Option[Todo]]]
          }
          Some(todo.copy(completed = true, updated = now))
        }.recoverWith {
          case ex: Exception =>
            Future.failed(ex)
        }

      case None =>
        Future.successful(None)
    }.recoverWith {
      case ex: Exception =>
        Future.failed(ex)
    }
  }

  // Переключение статуса "не выполнена" задачи по id
  def uncompleteTask(id: Int): Future[Option[Todo]] = {
    validateId(id)

    todoDAO.getById(id).flatMap {
      case Some(todo) =>
        val completeInfo = models.TodoCompleteTask(completed = false, updated = now)

        todoDAO.completeTaskById(id, completeInfo).map { affectedRows =>
          if (affectedRows == 0) {
            Future.failed(new Exception(s"Не удалось отметить выполненой задачу с ID: $id")).asInstanceOf[Future[Option[Todo]]]
          }
          Some(todo.copy(completed = false, updated = now))
        }.recoverWith {
          case ex: Exception =>
            Future.failed(ex)
        }
      case None =>
        Future.successful(None)
    }.recoverWith {
      case ex: Exception =>
        Future.failed(ex)
    }
  }

  // Переключение статуса "выполнена" всем задачам
  def completeAll(): Future[Int] = {
    todoDAO.countActiveTasks.flatMap { count =>
      if (count == 0) {
        Future.successful(0)
      } else {
        todoDAO.completeAllTasks(desiredStatus = true, timestamp = now).recoverWith {
          case ex: Exception =>
            handleError[Int]( ex, "Ошибка при массовом завершении задач")
        }
      }
    }.recoverWith {
      case ex: Exception =>
        handleError[Int]( ex, "Общая ошибка при обработке массового завершения задач")
    }
  }

  // Переключение статуса "не выполнена" всем задачам
  def uncompleteAll(): Future[Int] = {
    todoDAO.countCompletedTasks.flatMap { count =>
      if (count == 0) {
        Future.successful(0)
      } else {
        todoDAO.completeAllTasks(desiredStatus = false, timestamp = now).recoverWith {
          case ex: Exception =>
            handleError[Int]( ex, "Ошибка при массовом отмене завершения задач")
        }
      }
    }.recoverWith {
      case ex: Exception =>
        handleError[Int]( ex, "Общая ошибка при обработке массового снятия отметки о завершении задач")
    }
  }

  // Удаление всех выполненных таск
  def deleteCompleted(): Future[Int] = {
    todoDAO.countCompletedTasks.flatMap { count =>
      if (count == 0) {
        Future.successful(0)
      } else {
        todoDAO.deleteCompleted(timestamp = now).recoverWith {
          case ex: Exception =>
            handleError[Int]( ex, "Ошибка при массовом удалении завершенных задач")
        }
      }
    }.recoverWith {
      case ex: Exception =>
        handleError[Int]( ex, "Общая ошибка при обработке массового удаления задач")
    }
  }
}