package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import org.slf4j.LoggerFactory
import io.sentry.Sentry

import services.TodoService
import models._

@Singleton
class TodoController @Inject()(
  cc: ControllerComponents,
  todoService: TodoService,
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  // Универсальный обработчик ошибок
  private def handleError(ex: Throwable, message: String): Future[Result] = {
    logger.error(s"$message: ${ex.getMessage}", ex)
    Sentry.captureException(ex)
    Future.successful(
      InternalServerError(Json.obj(
        "code" -> 500,
        "message" -> message
      ))
    )
  }

  // Проверка ID (должен быть > 0)
  private def validateId(id: Int): Option[Result] =
    if (id <= 0) Some(BadRequest(Json.obj("code" -> 400, "message" -> "ID должен быть положительным")))
    else None

  // Проверка title (не пустая строка, не только пробелы)
  private def validateTitle(title: String): Option[Result] =
    if (title.trim.isEmpty) Some(BadRequest(Json.obj("code" -> 400, "message" -> "Title не может быть пустым")))
    else None

  // Получение всех задач
  def getAllTasks: Action[AnyContent] = Action.async {
    todoService.getAll
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> todos)))
      .recoverWith { case ex => handleError(ex, "Ошибка при получении списка задач") }
  }

  // Получение задачи по id
  def getTask(id: Int): Action[AnyContent] = Action.async {
    validateId(id).map(Future.successful).getOrElse {
      todoService.getTask(id)
        .map {
          case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> todo))
          case None       => NotFound(Json.obj("code" -> 404, "message" -> s"Задача $id не найдена"))
        }
        .recoverWith { case ex => handleError(ex, s"Ошибка при получении задачи id=$id") }
    }
  }

  // Добавление новой задачи
  def addTask(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[TodoAddTask].fold(
      errors =>
        Future.successful(BadRequest(Json.obj("code" -> 400, "message" -> "Ошибка валидации", "errors" -> JsError.toJson(errors)))),
      task =>
        validateTitle(task.title).map(Future.successful).getOrElse {
          todoService.addTask(task)
            .map(todo => Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> todo)))
            .recoverWith { case ex => handleError(ex, "Ошибка при создании задачи") }
        }
    )
  }

  // Обновить задачу по id
  def updateTask(id: Int): Action[JsValue] = Action.async(parse.json) { implicit request =>
    validateId(id).map(Future.successful).getOrElse {
      request.body.validate[TodoUpdateTask].fold(
        errors => Future.successful(BadRequest(Json.obj("code" -> 400, "message" -> "Ошибка валидации", "errors" -> JsError.toJson(errors)))),
        dto =>
          validateTitle(dto.title).map(Future.successful).getOrElse {
            todoService.updateTask(id, dto.title)
              .map { rowsUpdated =>
                if (rowsUpdated > 0)
                  Ok(Json.obj("code" -> 0, "message" -> s"Задача $id обновлена"))
                else
                  NotFound(Json.obj("code" -> 404, "message" -> s"Задача $id не найдена"))
              }
              .recoverWith { case ex => handleError(ex, s"Ошибка при обновлении задачи id=$id") }
          }
      )
    }
  }

  // Удалить задачу по id
  def deleteTask(id: Int): Action[AnyContent] = Action.async {
    validateId(id).map(Future.successful).getOrElse {
      todoService.deleteTask(id)
        .map { rowsAffected =>
          if (rowsAffected > 0) Ok(Json.obj("code" -> 0, "message" -> s"Задача $id удалена"))
          else NotFound(Json.obj("code" -> 404, "message" -> s"Задача $id не найдена"))
        }
        .recoverWith { case ex => handleError(ex, s"Ошибка при удалении задачи id=$id") }
    }
  }

  // Отметить задачу выполненной
  def completeTask(id: Int): Action[AnyContent] = Action.async {
    validateId(id).map(Future.successful).getOrElse {
      todoService.completeTask(id)
        .map(rows =>
          if (rows > 0) Ok(Json.obj("code" -> 0, "message" -> s"Задача $id выполнена"))
          else NotFound(Json.obj("code" -> 404, "message" -> s"Задача $id не найдена"))
        )
        .recoverWith { case ex => handleError(ex, s"Ошибка при завершении задачи id=$id") }
    }
  }

  // Отметить задачу невыполненной
  def uncompleteTask(id: Int): Action[AnyContent] = Action.async {
    validateId(id).map(Future.successful).getOrElse {
      todoService.uncompleteTask(id)
        .map(rows =>
          if (rows > 0) Ok(Json.obj("code" -> 0, "message" -> s"Задача $id отмечена как невыполненная"))
          else NotFound(Json.obj("code" -> 404, "message" -> s"Задача $id не найдена"))
        )
        .recoverWith { case ex => handleError(ex, s"Ошибка при отмене завершения задачи id=$id") }
    }
  }

  // Отметить все задачи как выполненные
  def completeAll: Action[AnyContent] = Action.async {
    todoService.completeAll()
      .map(rows => Ok(Json.obj("code" -> 0, "message" -> s"Выполнено $rows задач")))
      .recoverWith { case ex => handleError(ex, "Ошибка при завершении всех задач") }
  }

  // Отметить все задачи как невыполненные
  def uncompleteAll: Action[AnyContent] = Action.async {
    todoService.uncompleteAll()
      .map(rows => Ok(Json.obj("code" -> 0, "message" -> s"Отменено выполнение $rows задач")))
      .recoverWith { case ex => handleError(ex, "Ошибка при отмене завершения всех задач") }
  }

  // Удалить все выполненные задачи (soft delete)
  def deleteCompleted(): Action[AnyContent] = Action.async {
    todoService.deleteCompleted()
      .map(rows => Ok(Json.obj("code" -> 0, "message" -> s"Удалено $rows выполненных задач")))
      .recoverWith { case ex => handleError(ex, "Ошибка при удалении завершённых задач") }
  }
}