package dk.lokallykke.client.util.editor

import typings.editorjsEditorjs._

import scala.scalajs.js
import scala.scalajs.js.Object.create
import typings.editorjsEditorjs.mod
import typings.editorjsEditorjs.editorConfigMod

object Editor {

  object EditorLib extends js.Object {
    private val reqEditor = typings.editorjsEditorjs.editorjsEditorjsRequire
    def load() = reqEditor

  }
  EditorLib.load()

  def apply(insertId : String) : mod.EditorJS = {
    val config = editorConfigMod.EditorConfig().setHolder(insertId)
      .setOnReady(() => {
        println(s"Habla durante... Estoy listo")
      })
    val editor = new mod.default(config)
    editor
  }

  //Save



}
