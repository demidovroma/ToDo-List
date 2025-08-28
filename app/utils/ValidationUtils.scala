package utils

import models.dto.ValidationError
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.mvc.Results._
import models.implicits.JsonFormats._

import scala.concurrent.{ExecutionContext, Future}

object ValidationUtils {

  def validateId(id: Int)(implicit ec: ExecutionContext): Future[Either[Result, Unit]] =
    if (id <= 0)
      Future.successful(Left(BadRequest(Json.toJson(
        ValidationError(400, "ID должен быть положительным")
      ))))
    else
      Future.successful(Right(()))

  def validateTitle(json: JsValue)(implicit ec: ExecutionContext): Future[Either[Result, String]] =
    (json \ "title").asOpt[String] match {
      case None =>
        Future.successful(Left(BadRequest(Json.toJson(
          ValidationError(400, "Поле 'title' обязательно для заполнения")
        ))))

      case Some(title) if title.trim.isEmpty =>
        Future.successful(Left(BadRequest(Json.toJson(
          ValidationError(400, "Title не может быть пустым")
        ))))

      case Some(title) =>
        Future.successful(Right(title.trim))
    }
}
