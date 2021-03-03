package dk.lokallykke.client.util

import org.scalajs.dom
import org.scalajs.dom.document

object CommonUtil {



  def updateNode(node : dom.Node, text : String) = {
    node.innerText = text
  }

}
