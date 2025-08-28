package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

import services.TodoService
import models.dto.{TodoAdd, TodoUpdate, ValidationError}
import utils.{ErrorHandler, ValidationUtils}
import models.implicits.JsonFormats._

@Singleton
class TodoController @Inject()(
  cc: ControllerComponents,
  todoService: TodoService
)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with ErrorHandler {

  // Получение всех задач
  def getAll: Action[AnyContent] = Action.async {
    todoService.getAll
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> todos)))
      .recoverWith { case ex => handleError(ex, "Ошибка при получении списка задач") }
  }

  // Получить активные задачи
  def getActive: Action[AnyContent] = Action.async {
    todoService.getActive
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> todos)))
      .recoverWith { case ex => handleError(ex, "Ошибка при получении активных задач") }
  }

  // Получить выполненные задачи
  def getCompleted: Action[AnyContent] = Action.async {
    todoService.getCompleted
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> todos)))
      .recoverWith { case ex => handleError(ex, "Ошибка при получении выполненных задач") }
  }

  // Получение задачи по id
  def getById(id: Int): Action[AnyContent] = Action.async {
    ValidationUtils.validateId(id).flatMap {
      case Left(errorResult) => Future.successful(errorResult)
      case Right(_) =>
        todoService.getById(id)
          .map {
            case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> todo))
            case None       => NotFound(Json.toJson(ValidationError(404, s"Задача $id не найдена")))
          }
          .recoverWith { case ex => handleError(ex, s"Ошибка при получении задачи id=$id") }
    }
  }

  // Добавление новой задачи
  def add(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    ValidationUtils.validateTitle(request.body).flatMap {
      case Left(errorResult) => Future.successful(errorResult)
      case Right(_) =>
        request.body.validate[TodoAdd].fold(
          errors =>
            Future.successful(
              BadRequest(Json.toJson(
                ValidationError(400, "Некорректные данные для создания задачи: " + JsError.toJson(errors).toString())
              ))
            ),
          task =>
            todoService.add(task)
              .map {
                case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> "Задача успешно создана", "data" -> todo))
                case None       => InternalServerError(Json.toJson(ValidationError(500, "Не удалось создать задачу")))
              }
              .recoverWith { case ex => handleError(ex, s"Ошибка при создании задачи") }
        )
    }
  }

  // Обновить задачу по id
  def update(id: Int): Action[JsValue] = Action.async(parse.json) { implicit request =>
    ValidationUtils.validateTitle(request.body).flatMap {
      case Left(errorResult) => Future.successful(errorResult)
      case Right(_) =>
        request.body.validate[TodoUpdate].fold(
          errors =>
            Future.successful(
              BadRequest(Json.toJson(
                ValidationError(400, "Некорректные данные для обновления задачи: " + JsError.toJson(errors).toString())
              ))
            ),
          task =>
            todoService.update(id, task.title)
              .map {
                case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> s"Задача $id обновлена", "data" -> todo))
                case None       => NotFound(Json.toJson(ValidationError(404, s"Задача $id не найдена")))
              }
              .recoverWith { case ex => handleError(ex, s"Ошибка при обновлении задачи id=$id") }
        )
    }
  }

  // Удалить задачу по id
  def delete(id: Int): Action[AnyContent] = Action.async {
    ValidationUtils.validateId(id).flatMap {
      case Left(errorResult) => Future.successful(errorResult)
      case Right(_) =>
        todoService.delete(id)
          .map {
            case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> s"Задача $id удалена", "data" -> todo))
            case _          => NotFound(Json.toJson(ValidationError(404, s"Задача $id не найдена")))
          }
          .recoverWith { case ex => handleError(ex, s"Ошибка при удалении задачи id=$id") }
    }
  }

  // Отметить задачу выполненной
  def complete(id: Int): Action[AnyContent] = Action.async {
    ValidationUtils.validateId(id).flatMap {
      case Left(errorResult) => Future.successful(errorResult)
      case Right(_) =>
        todoService.complete(id)
          .map {
            case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> s"Задача $id выполнена", "data" -> todo))
            case _          => NotFound(Json.toJson(ValidationError(404, s"Задача $id не найдена")))
          }
          .recoverWith { case ex => handleError(ex, s"Ошибка при завершении задачи id=$id") }
    }
  }

  // Отметить задачу невыполненной
  def uncomplete(id: Int): Action[AnyContent] = Action.async {
    ValidationUtils.validateId(id).flatMap {
      case Left(errorResult) => Future.successful(errorResult)
      case Right(_) =>
        todoService.uncomplete(id)
          .map {
            case Some(todo)  => Ok(Json.obj("code" -> 0, "message" -> s"Задача $id отмечена как невыполненная", "data" -> todo))
            case _           => NotFound(Json.toJson(ValidationError(404, s"Задача $id не найдена")))
          }
          .recoverWith { case ex => handleError(ex, s"Ошибка при отмене завершения задачи id=$id") }
    }
  }

  // Отметить все задачи как выполненные
  def completeAll: Action[AnyContent] = Action.async {
    todoService.completeAll()
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> s"Выполнено ${todos.size} задач(и)", "data" -> todos)))
      .recoverWith { case ex => handleError(ex, "Ошибка при завершении всех задач") }
  }

  // Отметить все задачи как невыполненные
  def uncompleteAll: Action[AnyContent] = Action.async {
    todoService.uncompleteAll()
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> s"Отменено выполнение ${todos.size} задач(и)", "data" -> todos)))
      .recoverWith { case ex => handleError(ex, "Ошибка при отмене завершения всех задач") }
  }

  // Удалить все выполненные задачи
  def deleteCompleted(): Action[AnyContent] = Action.async {
    todoService.deleteCompleted()
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> s"Удалено ${todos.size} выполненных задач(и)", "data" -> todos)))
      .recoverWith { case ex => handleError(ex, "Ошибка при удалении завершённых задач") }
  }
}