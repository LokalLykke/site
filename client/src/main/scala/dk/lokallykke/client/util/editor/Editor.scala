package dk.lokallykke.client.util.editor

import dk.lokallykke.client.Locations
import org.scalablytyped.runtime.StringDictionary
import org.scalablytyped.runtime.StringDictionary._
import typings.editorjsEditorjs._
import typings.editorjsEditorjs.anon.Config

import scala.scalajs.js
import scala.scalajs.js.Object.create
import typings.editorjsEditorjs.mod
import typings.editorjsEditorjs.editorConfigMod
import typings.editorjsEditorjs.toolConfigMod.ToolConfig
import typings.editorjsEditorjs.toolSettingsMod.ToolSettings
import typings.editorjsEditorjs.toolsMod.ToolConstructable

import scala.scalajs.js.annotation.JSImport

object Editor {

  object EditorLib extends js.Object {
    private val reqEditor = typings.editorjsEditorjs.editorjsEditorjsRequire
    def load() = reqEditor

  }
  EditorLib.load()

  val tools = toolSettingsMod.ToolboxConfig.apply()

  def apply(insertId : String) : mod.EditorJS = {
    val config = editorConfigMod.EditorConfig()
      .setHolder(insertId)
      .setHideToolbar(false)
      .setTools(toolSettings)
    val editor = new mod.default(config)
    editor
  }

  /*def toolSettings = StringDictionary(
    "tools" -> StringDictionary(
      "header" -> StringDictionary(
        "class" -> "Header",
        "config" -> StringDictionary(
          "placeholder" -> "Enter a heading",
          "levels" -> js.Array(2,3,4,5),
          "defaultLevel" -> 3
        )
      ),
      "list" -> StringDictionary(
        "class" -> "List",
        "inlineToolbar" -> true
      )
    )
  )*/

  @js.native
  @JSImport("@editorjs/list", JSImport.Namespace)
  object ListTool extends ToolConstructable

  @js.native
  @JSImport("@editorjs/header", JSImport.Namespace)
  object HeaderTool extends ToolConstructable

  @js.native
  @JSImport("@editorjs/image", JSImport.Namespace)
  object ImageTool extends ToolConstructable


  def toolSettings: StringDictionary[ToolSettings[_]]  = StringDictionary(
    "list" -> toolSettingsMod.ToolSettings[Config](ListTool)
      .setInlineToolbar(true),
    "header" -> toolSettingsMod.ToolSettings[Config](HeaderTool)
      .setConfig(js.Dynamic.literal(
        "config" -> js.Dynamic.literal(
          "placeholder" -> "Enter a heading",
          "levels" -> js.Array(2,3,4,5),
          "defaultLevel" -> 3
        )
      ).asInstanceOf[ToolConfig[Config]]
    ),
    "image" -> toolSettingsMod.ToolSettings[Config](ImageTool)
      .setConfig(js.Dynamic.literal(
        "config" -> js.Dynamic.literal(
          "endpoints" -> js.Dynamic.literal(
            "byFile" -> Locations.Pages.saveImage
          )
        )
      ).asInstanceOf[ToolConfig[Config]]

    )


  )

  //Save



}
