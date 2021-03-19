package dk.lokallykke.client.util

import dk.lokallykke.client.util.CommonUtil.$i
import org.querki.jquery.$

object Validation {

  def validate(nonEmptyTextInputs : Seq[String] = Nil) : Boolean = {
    var ret = true
    nonEmptyTextInputs.foreach {
      case inp => {
        ret = ret & validate(inp, nonEmptyText)
      }
    }
    ret
  }

  def validateAndPerform[A](nonEmptyTextInputs : Seq[String] = Nil, afterwards : () => A) : Option[A] = {
    if(validate(nonEmptyTextInputs))
      Some(afterwards())
    else None
  }

  private def fieldName(id : String) = {
    var nam = id
    $(s"label[for='$id']").foreach(el => nam = $(el).text())
    nam
  }

  private def nonEmptyText(id : String) = {
    var nam = fieldName(id)
    val value = $i(id).value()
    if(value != null && value.toString.trim.size > 0) None
    else Some(s"Feltet $nam må ikke være tomt")
  }

  private def validate(inputId : String, func : String => Option[String]) : Boolean = {
    var ret = true
    $i(inputId).siblings("[usage='validation']").remove()
    func(inputId) match {
      case None => $i(inputId).parent().append(
        $("<div class='valid-feedback' usage='validation'>").text("Alt OK")
      )
      case Some(err) => {
        $i(inputId).parent().append(
          $("<div class='invalid-feedback' usage='validation'>").text(err)
        )
        ret = false
      }
    }
    ret
  }

}
