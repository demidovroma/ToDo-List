package controllers

import javax.inject._
import play.api.mvc._
import io.sentry.Sentry

@Singleton
class SentryController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def testSentry: Action[AnyContent] = Action { implicit request =>
    val enabled = Sentry.isEnabled

    if (enabled) {
      val ex = new RuntimeException("Тестовая ошибка (handled) для Sentry")
      Sentry.captureException(ex)
      Ok("Ошибка отправлена в Sentry.")
    } else {
      BadRequest("Sentry не активен")
    }
  }
}
