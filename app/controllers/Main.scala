package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class Main @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def RunApp(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }
}