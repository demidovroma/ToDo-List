package services

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

import daos.TodoDAO
import models.dto.{TodoAdd, TodoResponse}

@Singleton
class TodoService @Inject()(
  todoDAO: TodoDAO
)(implicit ec: ExecutionContext) {

  // Получить все задачи
  def getAll: Future[Seq[TodoResponse]] =
    todoDAO.getAll.map(_.map(TodoResponse.fromDb))

  // Получить только активные (не выполненные) задачи
  def getActive: Future[Seq[TodoResponse]] =
    todoDAO.getActive.map(_.map(TodoResponse.fromDb))

  // Получить только выполненные задачи
  def getCompleted: Future[Seq[TodoResponse]] =
    todoDAO.getCompleted.map(_.map(TodoResponse.fromDb))

  // Получение задачи по id
  def getById(id: Int): Future[Option[TodoResponse]] =
    todoDAO.getById(id).map(_.map(TodoResponse.fromDb))

  // Добавить задачу
  def add(task: TodoAdd): Future[Option[TodoResponse]] =
    todoDAO.add(task).map(_.map(TodoResponse.fromDb))

  // Обновление title задачи
  def update(id: Int, title: String): Future[Option[TodoResponse]] =
    todoDAO.update(id, title).map(_.map(TodoResponse.fromDb))

  // Удалить задачу (deleted=true)
  def delete(id: Int): Future[Option[TodoResponse]] =
    todoDAO.delete(id).map(_.map(TodoResponse.fromDb))

  // Отметить задачу как выполненную
  def complete(id: Int): Future[Option[TodoResponse]] =
    todoDAO.complete(id).map(_.map(TodoResponse.fromDb))

  // Отметить задачу как невыполненную
  def uncomplete(id: Int): Future[Option[TodoResponse]] =
    todoDAO.uncomplete(id).map(_.map(TodoResponse.fromDb))

  // Отметить все задачи как выполненные
  def completeAll(): Future[Seq[TodoResponse]] =
    todoDAO.completeAll().map(_.map(TodoResponse.fromDb))

  // Отметить все задачи как невыполненные
  def uncompleteAll(): Future[Seq[TodoResponse]] =
    todoDAO.uncompleteAll().map(_.map(TodoResponse.fromDb))

  // Удалить все выполненные задачи (soft delete)
  def deleteCompleted(): Future[Seq[TodoResponse]] =
    todoDAO.deleteCompleted().map(_.map(TodoResponse.fromDb))
}
