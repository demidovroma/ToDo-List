package daos

import java.sql.Timestamp
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import models._

class TodoDAO @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) {

  val db = dbConfigProvider.get[JdbcProfile].db
  val profile = dbConfigProvider.get[JdbcProfile].profile

  import profile.api._

  val todos = TableQuery[Todos]

  // Таблица "todos"
  class Todos(tag: Tag) extends Table[Todo](tag, "todos") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def completed = column[Boolean]("completed")
    def created = column[Timestamp]("created")
    def updated = column[Timestamp]("updated")
    def deleted = column[Boolean]("deleted")

    def * = (id, title, completed, created, updated, deleted).<>(
      (tuple: (Int, String, Boolean, Timestamp, Timestamp, Boolean)) => Todo(tuple._1, tuple._2 , tuple._3 , tuple._4 , tuple._5 , tuple._6),
      (todo: Todo) => Some((todo.id,todo.title,todo.completed,todo.created,todo.updated,todo.deleted))
    )
  }

  // Получить все задачи
  def getAll: Future[Seq[Todo]] = {
    db.run(todos.result)
  }

  // Получения такси по id
  def getById(id: Int): Future[Option[Todo]] = {
    db.run(todos.filter(_.id === id).result.headOption)
  }

  // Создать новую задачу
  def addTask(TodoAddTask: models.TodoAddTask): Future[Todo] = {
    val now = new Timestamp(System.currentTimeMillis())
    val newTodo = Todo(
      id = 0,
      title = TodoAddTask.title,
      completed = false,
      created = now,
      updated = now,
      deleted = false
    )
    val insertQuery = (todos returning todos.map(_.id) into ((todo, id) => todo.copy(id = id))) += newTodo
    db.run(insertQuery)
  }

  // Метод обновления title такси по id
  def updateTask(id: Int, updateInfo: models.TodoUpdateTask): Future[Int] = {
    val query = for { t <- todos if t.id === id } yield (t.title, t.updated)
    db.run(query.update((updateInfo.title, updateInfo.updated)))
  }

  // Метод для "удаления" (установка deleted в true)
  def deleteTaskById(id: Int, deleteInfo: models.TodoDeleteTask): Future[Int] = {
    val query = for { t <- todos if t.id === id } yield (t.deleted, t.updated)
    db.run(query.update((deleteInfo.deleted, deleteInfo.updated)))
  }

  // Метод для переключения в статус выполненой задачи
  def completeTaskById(id: Int, completeInfo: models.TodoCompleteTask): Future[Int] = {
    val query = for { t <- todos if t.id === id } yield (t.completed, t.updated)
    db.run(query.update((completeInfo.completed, completeInfo.updated)))
  }

  // Метод для переключения в статус "выполнеа/не выполнена" всех задач
  def completeAllTasks(desiredStatus: Boolean, timestamp: Timestamp): Future[Int] = {
    val query = todos.filter(_.completed =!= desiredStatus)
      .filter(t => (t.completed =!= desiredStatus) && (t.deleted === false))
      .map(t => (t.completed, t.updated))
      .update((desiredStatus, timestamp))
    db.run(query)
  }

  // Метод удаления всех выолненных задач
  def deleteCompleted(timestamp: Timestamp): Future[Int] = {
    val task = models.TodoDeleteTask(deleted = true, updated = timestamp)
    val query = todos.filter(_.completed === true)
      .map(t => (t.deleted, t.updated))
      .update((task.deleted, task.updated))
    db.run(query)
  }
}