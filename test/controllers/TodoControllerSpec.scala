package controllers

import scala.concurrent.{ExecutionContext, Future}

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{Materializer, SystemMaterializer}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

import models.dto.{TodoAdd, TodoResponse}
import models.implicits.JsonFormats._
import utils.TestHelpers._
import utils.Messages.Todo._
import services.TodoService

class TodoControllerSpec
  extends PlaySpec
    with MockitoSugar
    with Results
    with BeforeAndAfterAll {

  implicit val system: ActorSystem  = ActorSystem("TestSystem")
  implicit val mat: Materializer    = SystemMaterializer(system).materializer
  implicit val ec: ExecutionContext = system.dispatcher

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  private val mockService: TodoService = mock[TodoService]
  private val controller               = new TodoController(Helpers.stubControllerComponents(), mockService)(ec)

  private val todoResp: TodoResponse     = TodoResponse(1, "New task", completed = false)
  private val completedResp: TodoResponse = todoResp.copy(completed = true)
  private val uncompletedResp: TodoResponse = todoResp.copy(completed = false)

  private def call[A](action: EssentialAction, method: String, path: String, body: JsValue = Json.obj()) =
    action.apply(getRequest(method, path, body))

  "TodoController#getAll" should {
    "return 200 and list of todos when service returns data" in {
      when(mockService.getAll).thenReturn(Future.successful(Seq(todoResp)))

      val result = call(controller.getAll, GET, "/todos")
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(Seq(todoResp)), OkMsg)
    }

    "return 500 when service throws exception" in {
      when(mockService.getAll).thenReturn(Future.failed(new RuntimeException("boom")))

      val result = call(controller.getAll, GET, "/todos")
      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }

  "TodoController#getActive" should {
    "return 200 and active todos" in {
      when(mockService.getActive).thenReturn(Future.successful(Seq(todoResp)))

      val result = call(controller.getActive, GET, "/todos/active")
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(Seq(todoResp)), OkMsg)
    }
  }

  "TodoController#getCompleted" should {
    "return 200 and completed todos" in {
      when(mockService.getCompleted).thenReturn(Future.successful(Seq(completedResp)))

      val result = call(controller.getCompleted, GET, "/todos/completed")
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(Seq(completedResp)), OkMsg)
    }

    "return 200 and empty list if no completed todos" in {
      when(mockService.getCompleted).thenReturn(Future.successful(Seq.empty))

      val result = call(controller.getCompleted, GET, "/todos/completed")
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(Seq.empty[TodoResponse]), OkMsg)
    }
  }

  "TodoController#getById" should {
    "return 200 with todo if found" in {
      when(mockService.getById(1)).thenReturn(Future.successful(Some(todoResp)))

      val result = call(controller.getById(1), GET, "/todos/1")
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(todoResp), OkMsg)
    }

    "return 404 if not found" in {
      when(mockService.getById(2)).thenReturn(Future.successful(None))

      val result = call(controller.getById(2), GET, "/todos/2")
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe expectedOkResponse(404, JsNull, NotFoundMsg(2))
    }

    "return 400 if id is invalid" in {
      val result = call(controller.getById(0), GET, "/todos/0")
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe expectedOkResponse(400, JsNull, InvalidIdMsg)
    }
  }

  "TodoController#add" should {
    "return 200 and created task" in {
      when(mockService.add(any(classOf[TodoAdd]))).thenReturn(Future.successful(Some(todoResp)))

      val result = controller.add()(getRequest(POST, "/todos", Json.obj("title" -> "Test task")))
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(todoResp), CreatedMsg)
    }

    "return 400 when title is missing" in {
      val result = controller.add()(getRequest(POST, "/todos", Json.obj()))
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe expectedOkResponse(400, JsNull, TitleRequiredMsg)
    }

    "return 400 when title is empty string" in {
      val result = controller.add()(getRequest(POST, "/todos", Json.obj("title" -> "")))
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe expectedOkResponse(400, JsNull, TitleEmptyMsg)
    }

    "return 500 if service fails to create task" in {
      when(mockService.add(any[TodoAdd])).thenReturn(Future.successful(None))

      val result = controller.add()(getRequest(POST, "/todos", Json.obj("title" -> "Test task")))
      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe expectedOkResponse(500, JsNull, CreateFailedMsg)
    }

    "return 500 if service throws exception" in {
      when(mockService.add(any[TodoAdd])).thenReturn(Future.failed(new RuntimeException("boom")))

      val result = controller.add()(getRequest(POST, "/todos", Json.obj("title" -> "Test task")))
      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }

  "TodoController#update" should {
    "return 200 when task is updated" in {
      val updated = todoResp.copy(title = "Updated")
      when(mockService.update(eqTo(1), any[String])).thenReturn(Future.successful(Some(updated)))

      val result = controller.update(1)(getRequest(PUT, "/todos/1", Json.obj("title" -> "Updated")))
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(updated), UpdatedMsg(1))
    }

    "return 404 if not found" in {
      when(mockService.update(eqTo(2), any[String])).thenReturn(Future.successful(None))

      val result = controller.update(2)(getRequest(PUT, "/todos/2", Json.obj("title" -> "Updated")))
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe expectedOkResponse(404, JsNull, NotFoundMsg(2))
    }

    "return 400 if title is missing" in {
      val result = controller.update(1)(getRequest(PUT, "/todos/1", Json.obj()))
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe expectedOkResponse(400, JsNull, TitleRequiredMsg)
    }

    "return 400 if title is empty string" in {
      val result = controller.update(1)(getRequest(PUT, "/todos/1", Json.obj("title" -> "")))
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe expectedOkResponse(400, JsNull, TitleEmptyMsg)
    }
  }

  "TodoController#delete" should {
    "return 200 when deleted" in {
      when(mockService.delete(1)).thenReturn(Future.successful(Some(todoResp)))

      val result = controller.delete(1)(getRequest(DELETE, "/todos/1"))
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(todoResp), DeletedMsg(1))
    }

    "return 404 when not found" in {
      when(mockService.delete(2)).thenReturn(Future.successful(None))

      val result = controller.delete(2)(getRequest(DELETE, "/todos/2"))
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe expectedOkResponse(404, JsNull, NotFoundMsg(2))
    }

    "return 400 if id invalid" in {
      val result = controller.delete(0)(getRequest(DELETE, "/todos/0"))
      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe expectedOkResponse(400, JsNull, InvalidIdMsg)
    }
  }

  "TodoController#complete" should {
    "return 200 when task completed" in {
      when(mockService.complete(1)).thenReturn(Future.successful(Some(completedResp)))

      val result = controller.complete(1)(getRequest(POST, "/todos/1/complete"))
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(completedResp), CompletedMsg(1))
    }

    "return 404 if task not found" in {
      when(mockService.complete(2)).thenReturn(Future.successful(None))

      val result = controller.complete(2)(getRequest(POST, "/todos/2/complete"))
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe expectedOkResponse(404, JsNull, NotFoundMsg(2))
    }
  }

  "TodoController#uncomplete" should {
    "return 200 when task uncompleted" in {
      when(mockService.uncomplete(1)).thenReturn(Future.successful(Some(uncompletedResp)))

      val result = controller.uncomplete(1)(getRequest(POST, "/todos/1/uncomplete"))
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(uncompletedResp), UncompletedMsg(1))
    }

    "return 404 if task not found" in {
      when(mockService.uncomplete(2)).thenReturn(Future.successful(None))

      val result = controller.uncomplete(2)(getRequest(POST, "/todos/2/uncomplete"))
      status(result) mustBe NOT_FOUND
      contentAsJson(result) mustBe expectedOkResponse(404, JsNull, NotFoundMsg(2))
    }
  }

  "TodoController#completeAll" should {
    "return 200 and completed list" in {
      val completedList = Seq(completedResp)
      when(mockService.completeAll()).thenReturn(Future.successful(completedList))

      val result = controller.completeAll(getRequest(POST, "/todos/completeAll"))
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(completedList), CompletedAllMsg(completedList.size))
    }

    "return 200 with empty list if no todos" in {
      when(mockService.completeAll()).thenReturn(Future.successful(Seq.empty))

      val result = controller.completeAll(getRequest(POST, "/todos/completeAll"))
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(Seq.empty[TodoResponse]), CompletedAllMsg(0))
    }
  }

  "TodoController#uncompleteAll" should {
    "return 200 and uncompleted list" in {
      val uncompletedList = Seq(uncompletedResp)
      when(mockService.uncompleteAll()).thenReturn(Future.successful(uncompletedList))

      val result = controller.uncompleteAll(getRequest(POST, "/todos/uncompleteAll"))
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(uncompletedList), UncompletedAllMsg(uncompletedList.size))
    }
  }

  "TodoController#deleteCompleted" should {
    "return 200 and deleted list" in {
      val deletedList = Seq(completedResp)
      when(mockService.deleteCompleted()).thenReturn(Future.successful(deletedList))

      val result = controller.deleteCompleted()(getRequest(DELETE, "/todos/deleteCompleted"))
      status(result) mustBe OK
      contentAsJson(result) mustBe expectedOkResponse(0, Json.toJson(deletedList), DeletedCompletedMsg(deletedList.size))
    }
  }
}
