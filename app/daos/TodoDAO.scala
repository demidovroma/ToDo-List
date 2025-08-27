package daos

import javax.inject._

import anorm._
import anorm.SqlParser.get
import java.sql.Connection

import modules.DatabaseModule
import models.{Todo, TodoAddTask}
import java.sql.Timestamp

@Singleton
class TodoDAO @Inject()(protected val dbModule: DatabaseModule) {

  // Парсер для одной строки таблицы TODOS
  private val todoParser: RowParser[Todo] = {

    implicit val columnToTimestamp: Column[Timestamp] =
      Column.nonNull { (value, meta) =>
        value match {
          case ts: Timestamp => Right(ts)
          case d: java.util.Date => Right(new Timestamp(d.getTime))
          case _ =>
            Left(TypeDoesNotMatch(s"Cannot convert $value to Timestamp for column ${meta.column}"))
        }
      }

    get[Int]("id") ~
      get[String]("title") ~
      get[Int]("completed") ~
      get[Timestamp]("created") ~
      get[Timestamp]("updated") ~
      get[Int]("deleted") map {
      case id ~ title ~ completed ~ created ~ updated ~ deleted =>
        Todo(
          id,
          title,
          completed != 0,
          created,
          updated,
          deleted != 0
        )
    }
  }

  // Получить все задачи
  def getAll: Seq[Todo] = {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"SELECT * FROM TODOS WHERE DELETED = 0 ORDER BY created DESC".as(todoParser.*)
    }
  }

  // Получения задачи по id
  def getById(id: Int): Option[Todo] = {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"SELECT * FROM TODOS WHERE ID = $id AND DELETED = 0".as(todoParser.singleOpt)
    }
  }

  // Создать новую задачу
  def addTask(task: TodoAddTask): Todo =
    dbModule.withConnection { implicit conn: Connection =>
      val now = new Timestamp(System.currentTimeMillis())

      val id: Option[Int] =
        SQL"""
        INSERT INTO TODOS (TITLE, COMPLETED, CREATED, UPDATED, DELETED)
        VALUES (${task.title}, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
      """.executeInsert(SqlParser.scalar[Int].singleOpt)

      Todo(
        id = id.getOrElse(0),
        title = task.title,
        completed = false,
        created = now,
        updated = now,
        deleted = false
      )
    }

  // Обновление title задачи
  def updateTask(id: Int, title: String): Int = {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET TITLE = $title, UPDATED = CURRENT_TIMESTAMP WHERE ID = $id".executeUpdate()
    }
  }

  // Удалить задачу (deleted=true)
  def deleteTask(id: Int): Int = {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET DELETED = 1, UPDATED = CURRENT_TIMESTAMP WHERE ID = $id".executeUpdate()
    }
  }

  // Отметить задачу как выполненную
  def completeTask(id: Int): Int = {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET COMPLETED = 1, UPDATED = CURRENT_TIMESTAMP WHERE ID = $id".executeUpdate()
    }
  }

  // Отметить задачу как невыполненную
  def uncompleteTask(id: Int): Int = {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET COMPLETED = 0, UPDATED = CURRENT_TIMESTAMP WHERE ID = $id".executeUpdate()
    }
  }

  // Отметить все задачи как выполненные
  def completeAll(): Int = {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET COMPLETED = 1, UPDATED = CURRENT_TIMESTAMP WHERE DELETED = 0 AND COMPLETED = 0".executeUpdate()
    }
  }

  // Отметить все задачи как невыполненные
  def uncompleteAll(): Int = {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET COMPLETED = 0, UPDATED = CURRENT_TIMESTAMP WHERE DELETED = 0 AND COMPLETED = 1".executeUpdate()
    }
  }

  // Удалить все выполненные задачи (soft delete)
  def deleteCompleted(): Int = {
    dbModule.withConnection { implicit conn: Connection =>
      SQL"UPDATE TODOS SET DELETED = 1, UPDATED = CURRENT_TIMESTAMP WHERE COMPLETED = 1".executeUpdate()
    }
  }
}