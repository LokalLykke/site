package dk.lokallykke.client.util.editor

import dk.lokallykke.client.Locations
import dk.lokallykke.client.util.JsExtensions._
import org.scalablytyped.runtime.StringDictionary
import typings.editorjsEditorjs._
import typings.editorjsEditorjs.anon.Config

import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.Object.create
import typings.editorjsEditorjs.mod
import typings.editorjsEditorjs.editorConfigMod
import typings.editorjsEditorjs.toolConfigMod.ToolConfig
import typings.editorjsEditorjs.toolSettingsMod.ToolSettings
import typings.editorjsEditorjs.toolsMod.ToolConstructable

import scala.concurrent.{Await, ExecutionContext}
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSImport

object Editor {

  object EditorLib extends js.Object {
    private val reqEditor = typings.editorjsEditorjs.editorjsEditorjsRequire
    def load() = reqEditor

  }
  EditorLib.load()

  val tools = toolSettingsMod.ToolboxConfig.apply()

  def apply(insertId : String, blocks : Seq[EditorData.Block] = Nil) : mod.EditorJS = {
    val config = editorConfigMod.EditorConfig()
      .setHolder(insertId)
      .setHideToolbar(false)
      .setTools(toolSettings)
      .setData(outputDataMod.OutputData(blocks.map(_.toDataInput.asInstanceOf[outputDataMod.OutputBlockData[String,scala.scalajs.js.Any]]).toJsArray))
    val editor = new mod.default(config)
    editor
  }

  @js.native
  @JSImport("@editorjs/list", JSImport.Namespace)
  object ListTool extends ToolConstructable

  @js.native
  @JSImport("@editorjs/header", JSImport.Namespace)
  object HeaderTool extends ToolConstructable

  @js.native
  @JSImport("@editorjs/image", JSImport.Namespace)
  object ImageTool extends ToolConstructable


  def toolSettings: StringDictionary[ToolSettings[scala.scalajs.js.Any]]  = StringDictionary(
    "list" -> toolSettingsMod.ToolSettings[Config](ListTool)
      .setInlineToolbar(false),
    "header" -> toolSettingsMod.ToolSettings[Config](HeaderTool)
      .setConfig(js.Dynamic.literal(
          "placeholder" -> "Enter a heading",
          "levels" -> js.Array(2,3,4,5),
          "defaultLevel" -> 3
      ).asInstanceOf[ToolConfig[Config]]
    ),
    "image" -> toolSettingsMod.ToolSettings[Config](ImageTool)
      .setConfig(js.Dynamic.literal(
          "endpoints" -> js.Dynamic.literal(
            "byFile" -> Locations.saveImage
          )
      ).asInstanceOf[ToolConfig[Config]]

    )
  ).asInstanceOf[StringDictionary[ToolSettings[scala.scalajs.js.Any]]]

  implicit class OutputDataParser(promise : Promise[outputDataMod.OutputData]) {
    private implicit val dt = 20.seconds

    def result = {
      implicit val ec = ExecutionContext.global
      val returnee = promise.toFuture.map {
        case outputData => outputData.blocks.toSeq.flatMap {
          case block => {
            val casted = block.asInstanceOf[ParsingObjects.Block]
            casted.data.toOption.toList.map {
              case dat => {
                import EditorData.BlockType
                val typ = BlockType(block.`type`)
                val ret = typ match {
                  case BlockType.Paragraph => EditorData.Block(blockType = typ.idStr, text = dat.text.toOption)
                  case BlockType.Header => EditorData.Block(blockType = typ.idStr, text = dat.text.toOption, level = dat.level.toOption)
                  case BlockType.List => EditorData.Block(blockType = typ.idStr, items = dat.items.toOption.map(_.toSeq), style = dat.style.toOption)
                  case BlockType.Image => EditorData.Block(blockType = typ.idStr, fileUrl = dat.file.toOption.flatMap(_.url.toOption), caption = dat.caption.toOption, withBorder = dat.withBackground.toOption, withBackground = dat.withBorder.toOption, stretched = dat.stretched.toOption)
                }
                ret
              }
            }

          }
        }
      }
      returnee
    }
  }

  object EditorData {
    object BlockType extends Enumeration {
      type BlockType = Value
      case class BlTyp(idStr : String, prettyString : String) extends super.Val {
        override def toString = prettyString
      }
      val Paragraph = BlTyp("paragraph", "Paragraph")
      val Header = BlTyp("header", "Header")
      val List = BlTyp("list", "List")
      val Image = BlTyp("image", "Image")

      val allVals = Seq(Paragraph, Header, List, Image)
      val valsMap = allVals.map(v => v.idStr -> v).toMap
      def apply(idStr : String) = valsMap(idStr)
    }

    case class Block(blockType : String, text : Option[String] = None, level : Option[Int] = None, style : Option[String] = None,
                     items : Option[Seq[String]] = None, fileUrl : Option[String] = None, caption : Option[String] = None,
                     withBorder : Option[Boolean] = None, stretched : Option[Boolean] = None, withBackground : Option[Boolean] = None) {
      def toDataInput = convertToBlockData(this)
    }


    def convertToBlockData(block : Block) = {
      import js.JSConverters._
      js.Dynamic.literal(
        "type" -> block.blockType,
        "data" -> (BlockType(block.blockType) match {
            case BlockType.Paragraph => js.Dynamic.literal(
              "text" -> block.text.orUndefined
            )
            case BlockType.Header => js.Dynamic.literal(
              "text" -> block.text.orUndefined,
              "level" -> block.level.orUndefined
            )
            case BlockType.List => js.Dynamic.literal(
              "items" -> block.items.map(_.toJSArray).orUndefined,
              "style" -> block.style.orUndefined
            )
            case BlockType.Image => js.Dynamic.literal(
              "file" -> js.Dynamic.literal(
                "url" -> block.fileUrl.orUndefined
              ),
              "caption" -> block.caption.orUndefined,
              "withBorder" -> block.withBackground.orUndefined,
              "withBackground" -> block.withBackground.orUndefined,
              "stretched" -> block.stretched.orUndefined
            )
          })
      )
    }
  }

  object ParsingObjects {

    trait BlockDataFile extends js.Object {
      def url : js.UndefOr[String]
    }

    trait BlockData extends js.Object {
      def text : js.UndefOr[String]
      def level : js.UndefOr[Int]
      def style : js.UndefOr[String]
      def items : js.UndefOr[js.Array[String]]
      def file : js.UndefOr[BlockDataFile]
      def caption : js.UndefOr[String]
      def withBorder : js.UndefOr[Boolean]
      def stretched : js.UndefOr[Boolean]
      def withBackground : js.UndefOr[Boolean]

    }

    trait Block extends js.Object {
      def `type` : String
      def data : js.UndefOr[BlockData]
    }
  }





}
