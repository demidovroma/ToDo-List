// API для: добавления/изменения/удаления элементов списка
// Отмечать элемент списка как выполненный, и отменять это действие
// Пометить как выполненное/невыполненное все элементы списка
// Очистка выполненных (можно удалять, либо помечать как deleted=true в базе)
// Все результаты операции должны сохраняться в БД
// Все API желательно покрыть unit тестами
// Приложение должно иметь настроенный мониторинг ошибок через Sentry (ошибки должны отображаться здесь)

package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

import services.TodoService

@Singleton
class TodoController @Inject() (
  val cc: ControllerComponents,
  val todoService: TodoService,
  implicit val ec: ExecutionContext
) extends AbstractController(cc) {

  // Получение всех таск
  def getAllTasks: Action[AnyContent] = Action.async { implicit request =>
    todoService.getAll.map { todos =>
      Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> todos))
    }
  }

  // Получение одной таски по id
  def getTasks(id: Int): Action[AnyContent] = Action.async { implicit request =>
    todoService.getTask(id).map {
      case Some(todo) =>
        Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> Json.toJson(todo)))
      case None =>
        NotFound(Json.obj("code" -> 2, "message" -> s"Task with id $id not found"))
    }
  }

  // Добавление новой задачи
  def addTask(): Action[JsValue] = Action.async(parse.json) { request =>
    val titleOpt = (request.body \ "title").asOpt[String]

    titleOpt match {
      case Some(title) =>
        if (title.trim.isEmpty) {
          Future.successful(BadRequest(Json.obj("code" -> 1, "message" -> "Empty 'title'!")))
        } else {
          todoService.addTask(title).map { todo =>
            Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> Json.toJson(todo)))
          }
        }
      case None =>
        Future.successful(BadRequest(Json.obj("code" -> 1, "message" -> "Missing 'title'!")))
    }
  }

  // Обновить задачу по id
  def updateTask(id: Int): Action[JsValue] = Action.async(parse.json) { request =>
    val titleOpt = (request.body \ "title").asOpt[String]

    titleOpt match {
      case Some(title) =>
        if (title.trim.isEmpty) {
          Future.successful(BadRequest(Json.obj("code" -> 1, "message" -> "Empty 'title'!")))
        } else {
          todoService.updateTask(id, title).map {
            case Some(todo) =>
              Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> Json.toJson(todo)))
            case None =>
              NotFound(Json.obj("code" -> 2, "message" -> s"Task with id $id not found"))
          }
        }
      case None =>
        Future.successful(BadRequest(Json.obj("code" -> 1, "message" -> "Missing 'title'!")))
    }
  }

  // Удалить задачу по id
  def deleteTask(id: Int): Action[AnyContent] = Action.async { request =>
    todoService.deleteTask(id).map {
      case Some(todo) =>
        Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> Json.toJson(todo)))
      case None =>
        NotFound(Json.obj("code" -> 2, "message" -> s"Task with id $id not found"))
    }
  }

  // Переключение статуса "выполнена" задачи по id
  def completeTask(id: Int): Action[AnyContent] = Action.async { request =>
    todoService.completeTask(id).map {
      case Some(updatedTodo) =>
        Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> Json.toJson(updatedTodo)))
      case None =>
        NotFound(Json.obj("code" -> 2, "message" -> s"Task with id $id not found"))
    }
  }

  // Переключение статуса "выполнена" задачи по id
  def uncompleteTask(id: Int): Action[AnyContent] = Action.async { request =>
    todoService.uncompleteTask(id).map {
      case Some(updatedTodo) =>
        Ok(Json.obj("code" -> 0, "message" -> "Ok", "data" -> Json.toJson(updatedTodo)))
      case None =>
        NotFound(Json.obj("code" -> 2, "message" -> s"Task with id $id not found"))
    }
  }

  // Переключение статуса "выполнена" всем задачам
  def completeAll(): Action[AnyContent] = Action.async { implicit request =>
    todoService.completeAll().map { updatedCount =>
      Ok(Json.obj("code" -> 0, "message" -> s"Marked $updatedCount tasks as completed"
      ))
    }
  }

  // Переключение статуса "не выполнена" всем задачам
  def uncompleteAll(): Action[AnyContent] = Action.async { implicit request =>
    todoService.uncompleteAll().map { updatedCount =>
      Ok(Json.obj("code" -> 0, "message" -> s"Marked $updatedCount tasks as uncompleted"
      ))
    }
  }

  // Удаление выполненых задач
  def deleteCompleted(): Action[AnyContent] = Action.async { implicit request =>
    todoService.deleteCompleted().map { updatedCount =>
      Ok(Json.obj("code" -> 0, "message" -> s"Marked $updatedCount tasks as deleted"))
    }
  }
}