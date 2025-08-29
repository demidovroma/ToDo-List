package utils

import models.dto.ValidationError
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.mvc.Results._
import scala.concurrent.{ExecutionContext, Future}

import models.implicits.JsonFormats._
import utils.Messages.Todo._

object ValidationUtils {

  def validateId(id: Int)(implicit ec: ExecutionContext): Future[Either[Result, Unit]] =
    if (id <= 0)
      Future.successful(Left(BadRequest(Json.toJson(
        ValidationError(400, InvalidIdMsg)
      ))))
    else
      Future.successful(Right(()))

  def validateTitle(json: JsValue)(implicit ec: ExecutionContext): Future[Either[Result, String]] =
    (json \ "title").asOpt[String] match {
      case None =>
        Future.successful(Left(BadRequest(Json.toJson(
          ValidationError(400, TitleRequiredMsg)
        ))))

      case Some(title) if title.trim.isEmpty =>
        Future.successful(Left(BadRequest(Json.toJson(
          ValidationError(400, TitleEmptyMsg)
        ))))

      case Some(title) =>
        Future.successful(Right(title.trim))
    }
}
