package controllers

import javax.inject.Inject
import play.api.mvc._

class AccountingController  @Inject() (cc : ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    implicit request : Request[AnyContent] => {
      Ok(views.html.accounting())
    }
  }

}
