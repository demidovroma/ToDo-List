package utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.libs.json._

object JsonImplicits {

  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    override def writes(ts: Timestamp): JsValue =
      JsString(dateFormat.format(ts))

    override def reads(json: JsValue): JsResult[Timestamp] = json match {
      case JsString(str) =>
        try {
          JsSuccess(new Timestamp(dateFormat.parse(str).getTime))
        } catch {
          case e: Exception => JsError(s"Invalid date format: $str")
        }
      case _ => JsError("Expected string for Timestamp")
    }
  }
}
