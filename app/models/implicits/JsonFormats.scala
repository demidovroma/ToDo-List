package models.implicits

import play.api.libs.json._
import models.db._
import models.dto._

trait JsonFormats {
  implicit val todoAddFormat: OFormat[TodoAdd] = Json.format[TodoAdd]
  implicit val todoUpdateFormat: OFormat[TodoUpdate] = Json.format[TodoUpdate]
  implicit val todoResponseFormat: OFormat[TodoResponse] = Json.format[TodoResponse]
  implicit val validationErrorFormat: OFormat[ValidationError] = Json.format[ValidationError]
}

object JsonFormats extends JsonFormats