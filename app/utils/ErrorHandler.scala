package utils

import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.Future
import org.slf4j.LoggerFactory

import models.dto.ValidationError
import models.implicits.JsonFormats._

trait ErrorHandler {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def handleError(ex: Throwable, message: String): Future[Result] = {
    logger.error(s"$message: ${ex.getMessage}", ex)
    Future.successful(
      Results.InternalServerError(Json.toJson(ValidationError(500, message)))
    )
  }
}
