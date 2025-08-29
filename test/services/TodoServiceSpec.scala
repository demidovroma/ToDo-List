package services

import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.{ExecutionContext, Future}

import models.db.Todo
import models.dto.{TodoAdd, TodoResponse}
import daos.TodoDAO

class TodoServiceSpec extends AsyncFunSuite with Matchers with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val mockDao   = mock[TodoDAO]
  private val service   = new TodoService(mockDao)

  private val todoDb         = Todo(1, "Test task", completed = false, deleted = false)
  private val todoDbCompleted= todoDb.copy(completed = true)
  private val todoDbDeleted  = todoDb.copy(deleted = true)

  private val todoResp          = TodoResponse.fromDb(todoDb)
  private val todoRespCompleted = TodoResponse.fromDb(todoDbCompleted)
  private val todoRespDeleted   = TodoResponse.fromDb(todoDbDeleted)

  test("getAll should return list of TodoResponse") {
    when(mockDao.getAll).thenReturn(Future.successful(Seq(todoDb)))

    service.getAll.map { result =>
      result shouldBe Seq(todoResp)
    }
  }

  test("getById should return Some(TodoResponse) if found") {
    when(mockDao.getById(1)).thenReturn(Future.successful(Some(todoDb)))

    service.getById(1).map { result =>
      result shouldBe Some(todoResp)
    }
  }

  test("getById should return None if not found") {
    when(mockDao.getById(2)).thenReturn(Future.successful(None))

    service.getById(2).map { result =>
      result shouldBe None
    }
  }

  test("add should return created TodoResponse when DAO succeeds") {
    val todoAdd = TodoAdd("New task")
    when(mockDao.add(todoAdd)).thenReturn(Future.successful(Some(todoDb)))

    service.add(todoAdd).map { result =>
      result shouldBe Some(todoResp)
    }
  }

  test("add should return None when DAO fails to create") {
    val todoAdd = TodoAdd("New task")
    when(mockDao.add(todoAdd)).thenReturn(Future.successful(None))

    service.add(todoAdd).map { result =>
      result shouldBe None
    }
  }

  test("update should return updated TodoResponse when DAO succeeds") {
    when(mockDao.update(1, "Updated")).thenReturn(Future.successful(Some(todoDb.copy(title = "Updated"))))

    service.update(1, "Updated").map { result =>
      result.map(_.title) shouldBe Some("Updated")
    }
  }

  test("update should return None when DAO returns None") {
    when(mockDao.update(2, "Updated")).thenReturn(Future.successful(None))

    service.update(2, "Updated").map { result =>
      result shouldBe None
    }
  }

  test("delete should return deleted TodoResponse when DAO succeeds") {
    when(mockDao.delete(1)).thenReturn(Future.successful(Some(todoDbDeleted)))

    service.delete(1).map { result =>
      result shouldBe Some(todoRespDeleted)
    }
  }

  test("delete should return None when DAO returns None") {
    when(mockDao.delete(2)).thenReturn(Future.successful(None))

    service.delete(2).map { result =>
      result shouldBe None
    }
  }

  test("complete should mark todo as completed") {
    when(mockDao.complete(1)).thenReturn(Future.successful(Some(todoDbCompleted)))

    service.complete(1).map { result =>
      result shouldBe Some(todoRespCompleted)
    }
  }

  test("complete should return None when DAO returns None") {
    when(mockDao.complete(2)).thenReturn(Future.successful(None))

    service.complete(2).map { result =>
      result shouldBe None
    }
  }

  test("uncomplete should mark todo as not completed") {
    when(mockDao.uncomplete(1)).thenReturn(Future.successful(Some(todoDb)))

    service.uncomplete(1).map { result =>
      result shouldBe Some(todoResp)
    }
  }

  test("uncomplete should return None when DAO returns None") {
    when(mockDao.uncomplete(2)).thenReturn(Future.successful(None))

    service.uncomplete(2).map { result =>
      result shouldBe None
    }
  }

  test("completeAll should return all completed todos") {
    when(mockDao.completeAll()).thenReturn(Future.successful(Seq(todoDbCompleted)))

    service.completeAll().map { result =>
      result shouldBe Seq(todoRespCompleted)
    }
  }

  test("uncompleteAll should return all uncompleted todos") {
    when(mockDao.uncompleteAll()).thenReturn(Future.successful(Seq(todoDb)))

    service.uncompleteAll().map { result =>
      result shouldBe Seq(todoResp)
    }
  }

  test("deleteCompleted should return deleted completed todos") {
    when(mockDao.deleteCompleted()).thenReturn(Future.successful(Seq(todoDbCompleted.copy(deleted = true))))

    service.deleteCompleted().map { result =>
      result shouldBe Seq(TodoResponse(1, "Test task", completed = true))
    }
  }
}
