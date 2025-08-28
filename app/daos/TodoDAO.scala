package daos

import javax.inject._

import anorm._
import java.sql.Connection

import scala.concurrent.{ExecutionContext, Future, blocking}

import modules.DatabaseModule
import models.db.Todo
import models.dto.TodoAdd
import models.implicits.ParserOps

@Singleton
class TodoDAO @Inject()(
  dbModule: DatabaseModule
)(implicit ec: ExecutionContext) extends ParserOps {

  // Получить все задачи
  def getAll: Future[Seq[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"SELECT * FROM TODOS WHERE DELETED = 0 ORDER BY ID DESC".as(todoParser.*)
    }
  })

  // Получить все активные задачи
  def getActive: Future[Seq[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"SELECT * FROM TODOS WHERE DELETED = 0 AND COMPLETED = 0".as(todoParser.*)
    }
  })

  // Получить все выполненные задачи
  def getCompleted: Future[Seq[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"SELECT * FROM TODOS WHERE DELETED = 0 AND COMPLETED = 1".as(todoParser.*)
    }
  })

  // Получение задачи по id
  def getById(id: Int): Future[Option[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"SELECT * FROM TODOS WHERE ID = $id AND DELETED = 0".as(todoParser.singleOpt)
    }
  })

  // Создать новую задачу
  def add(task: TodoAdd): Future[Option[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      val id: Option[Long] =
        SQL"""
          INSERT INTO TODOS (TITLE, COMPLETED, DELETED)
          VALUES (${task.title}, 0, 0)
        """.executeInsert()

      id.flatMap { newId =>
        SQL"SELECT * FROM TODOS WHERE ID = $newId".as(todoParser.singleOpt)
      }
    }
  })

  // Обновление title задачи
  def update(id: Int, title: String): Future[Option[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET TITLE = $title WHERE ID = $id".executeUpdate()
      SQL"SELECT * FROM TODOS WHERE ID = $id".as(todoParser.singleOpt)
    }
  })

  // Удалить задачу (deleted = true)
  def delete(id: Int): Future[Option[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET DELETED = 1 WHERE ID = $id".executeUpdate()
      SQL"SELECT * FROM TODOS WHERE ID = $id".as(todoParser.singleOpt)
    }
  })

  // Отметить задачу как выполненную
  def complete(id: Int): Future[Option[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET COMPLETED = 1 WHERE ID = $id".executeUpdate()
      SQL"SELECT * FROM TODOS WHERE ID = $id".as(todoParser.singleOpt)
    }
  })

  // Отметить задачу как невыполненную
  def uncomplete(id: Int): Future[Option[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET COMPLETED = 0 WHERE ID = $id".executeUpdate()
      SQL"SELECT * FROM TODOS WHERE ID = $id".as(todoParser.singleOpt)
    }
  })

  // Отметить все задачи как выполненные
  def completeAll(): Future[Seq[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET COMPLETED = 1 WHERE DELETED = 0 AND COMPLETED = 0".executeUpdate()
      SQL"SELECT * FROM TODOS WHERE DELETED = 0 AND COMPLETED = 1".as(todoParser.*)
    }
  })

  // Отметить все задачи как невыполненные
  def uncompleteAll(): Future[Seq[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET COMPLETED = 0 WHERE DELETED = 0 AND COMPLETED = 1".executeUpdate()
      SQL"SELECT * FROM TODOS WHERE DELETED = 0 AND COMPLETED = 0".as(todoParser.*)
    }
  })

  // Удалить все выполненные задачи (soft delete)
  def deleteCompleted(): Future[Seq[Todo]] = Future(blocking {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET DELETED = 1 WHERE COMPLETED = 1".executeUpdate()
      SQL"SELECT * FROM TODOS WHERE DELETED = 1 AND COMPLETED = 1".as(todoParser.*)
    }
  })
}