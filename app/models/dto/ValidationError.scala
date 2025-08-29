package models.dto

import play.api.libs.json._

case class ValidationError(code: Int, message: String, data: JsValue = JsNull)
