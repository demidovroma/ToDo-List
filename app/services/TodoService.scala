package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import models._
import daos.TodoDAO

@Singleton
class TodoService @Inject()(todoDAO: TodoDAO)(implicit ec: ExecutionContext) {

  // Получить все задачи
  def getAll: Future[Seq[Todo]] =
    Future { todoDAO.getAll }

  // Получения задачи по id
  def getTask(id: Int): Future[Option[Todo]] =
    Future { todoDAO.getById(id) }

  // Создать новую задачу
  def addTask(task: TodoAddTask): Future[Todo] =
    Future { todoDAO.addTask(task) }

  // Обновление title задачи
  def updateTask(id: Int, title: String): Future[Int] =
    Future { todoDAO.updateTask(id, title) }

  // Удалить задачу (deleted=true)
  def deleteTask(id: Int): Future[Int] =
    Future { todoDAO.deleteTask(id) }

  // Отметить задачу как выполненную
  def completeTask(id: Int): Future[Int] =
    Future { todoDAO.completeTask(id) }

  // Отметить задачу как невыполненную
  def uncompleteTask(id: Int): Future[Int] =
    Future { todoDAO.uncompleteTask(id) }

  // Отметить все задачи как выполненные
  def completeAll(): Future[Int] =
    Future { todoDAO.completeAll() }

  // Отметить все задачи как невыполненные
  def uncompleteAll(): Future[Int] =
    Future { todoDAO.uncompleteAll() }

  // Удалить все выполненные задачи (soft delete)
  def deleteCompleted(): Future[Int] =
    Future { todoDAO.deleteCompleted() }
}
