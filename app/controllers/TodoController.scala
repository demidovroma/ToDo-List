package controllers

import javax.inject._

import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

import services.TodoService
import models.dto.{TodoAdd, TodoUpdate, ValidationError}
import utils.{ErrorHandler, ValidationUtils}
import utils.Messages.Todo._
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
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> OkMsg, "data" -> todos)))
      .recoverWith { case ex => handleError(ex, GetAllErrorMsg) }
  }

  // Получить активные задачи
  def getActive: Action[AnyContent] = Action.async {
    todoService.getActive
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> OkMsg, "data" -> todos)))
      .recoverWith { case ex => handleError(ex, GetActiveErrorMsg) }
  }

  // Получить выполненные задачи
  def getCompleted: Action[AnyContent] = Action.async {
    todoService.getCompleted
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> OkMsg, "data" -> todos)))
      .recoverWith { case ex => handleError(ex, GetCompletedErrorMsg) }
  }

  // Получение задачи по id
  def getById(id: Int): Action[AnyContent] = Action.async {
    ValidationUtils.validateId(id).flatMap {
      case Left(errorResult) => Future.successful(errorResult)
      case Right(_) =>
        todoService.getById(id)
          .map {
            case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> OkMsg, "data" -> todo))
            case None       => NotFound(Json.toJson(ValidationError(404, NotFoundMsg(id))))
          }
          .recoverWith { case ex => handleError(ex, GetByIdErrorMsg(id)) }
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
                ValidationError(400, AddIncorrectDataErrorMsg + JsError.toJson(errors).toString())
              ))
            ),
          task =>
            todoService.add(task)
              .map {
                case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> CreatedMsg, "data" -> todo))
                case None       => InternalServerError(Json.toJson(ValidationError(500, CreateFailedMsg)))
              }
              .recoverWith { case ex => handleError(ex, CreatedErrorMsg) }
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
                ValidationError(400, UpdateIncorrectDataErrorMsg + JsError.toJson(errors).toString())
              ))
            ),
          task =>
            todoService.update(id, task.title)
              .map {
                case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> UpdatedMsg(id), "data" -> todo))
                case None       => NotFound(Json.toJson(ValidationError(404, NotFoundMsg(id))))
              }
              .recoverWith { case ex => handleError(ex, UpdatedErrorMsg(id)) }
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
            case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> DeletedMsg(id), "data" -> todo))
            case _          => NotFound(Json.toJson(ValidationError(404, NotFoundMsg(id))))
          }
          .recoverWith { case ex => handleError(ex, DeletedErrorMsg(id)) }
    }
  }

  // Отметить задачу выполненной
  def complete(id: Int): Action[AnyContent] = Action.async {
    ValidationUtils.validateId(id).flatMap {
      case Left(errorResult) => Future.successful(errorResult)
      case Right(_) =>
        todoService.complete(id)
          .map {
            case Some(todo) => Ok(Json.obj("code" -> 0, "message" -> CompletedMsg(id), "data" -> todo))
            case _          => NotFound(Json.toJson(ValidationError(404, NotFoundMsg(id))))
          }
          .recoverWith { case ex => handleError(ex, CompletedErrorMsg(id: Int)) }
    }
  }

  // Отметить задачу невыполненной
  def uncomplete(id: Int): Action[AnyContent] = Action.async {
    ValidationUtils.validateId(id).flatMap {
      case Left(errorResult) => Future.successful(errorResult)
      case Right(_) =>
        todoService.uncomplete(id)
          .map {
            case Some(todo)  => Ok(Json.obj("code" -> 0, "message" -> UncompletedMsg(id), "data" -> todo))
            case _           => NotFound(Json.toJson(ValidationError(404, NotFoundMsg(id))))
          }
          .recoverWith { case ex => handleError(ex, UncompletedErrorMsg(id)) }
    }
  }

  // Отметить все задачи как выполненные
  def completeAll: Action[AnyContent] = Action.async {
    todoService.completeAll()
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> CompletedAllMsg(todos.size), "data" -> todos)))
      .recoverWith { case ex => handleError(ex, CompletedAllErrorMsg) }
  }

  // Отметить все задачи как невыполненные
  def uncompleteAll: Action[AnyContent] = Action.async {
    todoService.uncompleteAll()
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> UncompletedAllMsg(todos.size), "data" -> todos)))
      .recoverWith { case ex => handleError(ex, UncompletedAllErrorMsg) }
  }

  // Удалить все выполненные задачи
  def deleteCompleted(): Action[AnyContent] = Action.async {
    todoService.deleteCompleted()
      .map(todos => Ok(Json.obj("code" -> 0, "message" -> DeletedCompletedMsg(todos.size), "data" -> todos)))
      .recoverWith { case ex => handleError(ex, DeletedCompletedErrorMsg) }
  }
}