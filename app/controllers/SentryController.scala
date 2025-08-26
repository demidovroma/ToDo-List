package controllers

import javax.inject.Inject

import io.sentry.Sentry
import play.api.mvc._

class SentryController @Inject() (
  val cc: ControllerComponents
) extends AbstractController(cc) {

  def testSentry: Action[AnyContent] = Action {
    val enabled = Sentry.isEnabled
    if (enabled) {
      Sentry.withScope { scope =>
        scope.setTag("test", "true")
        Sentry.captureMessage("Тестирование Sentry")
      }
      Ok("Событие отправлено")
    } else {
      BadRequest("Sentry не активен")
    }
  }
}
