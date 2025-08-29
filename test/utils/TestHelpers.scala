package utils

import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers.CONTENT_TYPE

object TestHelpers {
  def getRequest(method: String, url: String, body: JsValue = JsNull): FakeRequest[JsValue] =
    FakeRequest(method, url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withBody(body)

  def expectedOkResponse(code: Int, data: JsValue, message: String = "Ok"): JsObject =
    Json.obj("code" -> code, "message" -> message, "data" -> data)
}
