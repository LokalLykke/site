package dk.lokallykke.client.pages

import dk.lokallykke.client.Locations
import dk.lokallykke.client.Messages.Pages.PageShell
import dk.lokallykke.client.items.Items.availableTags
import dk.lokallykke.client.util.CommonUtil.$i
import dk.lokallykke.client.util.{Modal, Selector, Validation, WSConnector}
import dk.lokallykke.client.util.editor._
import dk.lokallykke.client.util.editor.Editor.OutputDataParser
import dk.lokallykke.client.util.JsExtensions._
import dk.lokallykke.client.util.tables.Column.{DateTimeColumn, DoubleColumn, ImageColumn, StringColumn}
import dk.lokallykke.client.viewmodel.items.ViewItem
import dk.lokallykke.client.viewmodel.pages.ViewPage
import io.circe.Json
import org.querki.jquery.{$, JQuery, JQueryEventObject}
import typings.editorjsEditorjs.mod

import scala.scalajs.js
import js.Thenable.Implicits._
import scala.concurrent.ExecutionContext
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.{Failure, Try}

@JSExportTopLevel("Pages")
object Pages {
  val ContentId = "pages-content"
  val FormId = "pages-form"
  val FormNameId = "pages-form-name"
  val FormDescriptionId = "pages-form-description"
  val FormTagsHolderId = "pages-form-tags-insert"
  val FormTagsId = "pages-form-tags"
  val FormExecuteFilterId = "pages-form-execute-tag-filter-button"
  val FormSaveButtonId = "pages-form-save-button"
  val FormDeleteButtonId = "pages-form-delete-button"
  val BlockEditorId = "pages-blocks-editor"
  val SidebarPagesInsertId = "pages-sidebar-pages-insert"
  val SidebarCreateButtonId = "pages-sidebar-create-button"


  var pageShells : Seq[PageShell] = Nil
  var allTags : Seq[String] = Nil
  var tagSelector : Option[typings.selectize.JQuery] = None
  var currentViewPage : Option[ViewPage] = None
  var editor : Option[mod.EditorJS] = None
  private implicit val contx = ExecutionContext.global


  @JSExport
  def main() : Unit = {

    $i(SidebarCreateButtonId).click((obj : JQueryEventObject) => {
      println("Creating a new page")
      clearSelection()
      makeViewPageSelection(ViewPage(-1L, "Ny side", None, Nil, Nil))
    })

    PageConnector.connectToServer
  }

  @JSExport
  def setTags(tags : String) : Unit = {
    setTags(tags.split(";"))
  }

  def setTags(tags : Seq[String]) : Unit = {
    allTags = tags
  }

  @JSExport
  def setPageShells(shellsString : String) : Unit = Try {
    import io.circe.parser._
    import io.circe.Decoder
    import io.circe.generic.semiauto._
    implicit val decoder : Decoder[PageShell] = deriveDecoder

    parse(shellsString) match {
      case Left(err) => println(err.message)
      case Right(js) => {
        val shells = js.asArray.get.map{i =>
          decode[PageShell](i.toString).toOption.get
        }
        setPageShells(shells)
      }
    }
  } match {
    case Failure(err) => err.printStackTrace()
    case _ =>
  }

  def setPageShells(newShells : Seq[PageShell]) = {
    pageShells = newShells
    $i(SidebarPagesInsertId).empty()
    pageShells.foreach {
      case sh => {
        val pageElem = $(s"<a class='list-group-item list-group-item-action' typ='items-nav' href='#' page-id='${sh.id}'>").text(sh.name)
        $i(SidebarPagesInsertId).append(
          pageElem
        )
        $(pageElem).click((obj : JQueryEventObject) => { PageConnector.requestPage(sh.id)})
      }
    }
  }

  def savePage() : Unit = {
    editor.foreach {
      case edit => {
        edit.save().result.onComplete {
          case tr => {
            tr.foreach {
              case blocks => {
                currentViewPage = currentViewPage.map(p => p.copy(blocks = blocks))
                currentViewPage.foreach(p => PageConnector.savePage(p))
              }
            }
          }
        }
      }
    }

  }

  def clearSelection() = {
    editor.foreach(ed => ed.destroy())
    $i(ContentId).empty()
    currentViewPage = None
    editor = None
  }

  def makeViewPageSelection(page : ViewPage) : Unit = {
    clearSelection()
    currentViewPage = Some(page)
    $i(ContentId).append(
      $("<div class='row'>").append(
        $("<div class='col-12'>").append(
          $(s"<form id='$FormId' class='border my-4 p-4 bg-light'>").append(
            $("<div class='form-row'>").append(
              $("<div class='form-group col-6'>").append(
                $(s"<label for='$FormNameId'>").text("Side-navn"),
                $(s"<input type='text' id='$FormNameId' class='form-control' placeholder='Navn' required>").value(page.name)
              ),
              $("<div class='form-group col-6'>").append(
                $(s"<label for='$FormDescriptionId'>").text("Beskrivelse"),
                $(s"<textarea type='text' id='$FormDescriptionId' class='form-control' placeholder='Navn' rows='3'>").text(page.description.getOrElse(""))
              )
            ),
            $("<div class='form-row'>").append(
              $(s"<div class='form-group col-12' id='$FormTagsHolderId'>").append(
                $(s"<label for='$FormTagsId'>").text("Mærkater")
              )
            ),
            $("<div class='form-row'>").append(
              $("<div class='form-group col-8'>").append(
                $(s"<button type='button' id='$FormExecuteFilterId' class='btn btn-outline-primary'>").text("Afprøv filter")
              ),
              $("<div class='form-group col-2'>").append(
                $(s"<button type='button' id='$FormDeleteButtonId' class='btn btn-outline-danger'>").text("Slet side")
              ),
              $("<div class='form-group col-2'>").append(
                $(s"<button type='button' id='$FormSaveButtonId' class='btn btn-outline-info'>").text("Gem")
              )
            )
          )
        )
      ),
      $("<div class='border p-5 bg-light'>").append(
        $(s"<div id='pages-blocks-editor'>")
      )
    )
    tagSelector = Some(Selector(FormTagsId, appendTo = $(s"#$FormTagsHolderId"), allTags, page.tags))
    bindEventHandlers()
    bindFormButtons()
    val blocks = page.blocks.map {
      case bl => Editor.EditorData.Block(bl.blockType, bl.text, bl.level, bl.style, bl.items, bl.fileUrl, bl.caption, bl.withBorder, bl.stretched, bl.withBackground)
    }
    editor = Some(Editor(BlockEditorId, blocks))
  }

  def showError(error : String) = {
    org.scalajs.dom.window.alert(error)
  }

  private def bindFormButtons() : Unit = {
    $i(FormExecuteFilterId).click((obj : JQueryEventObject) => {
      val tagString = $i(FormTagsId).value()
      if(tagString != null) {
        PageConnector.executeFilter(tagString.toString.split(";"))
      }
    })

    $i(FormSaveButtonId).click((obj : JQueryEventObject) => {
      Validation.validateAndPerform(List(FormNameId), () => {
        savePage()
      })
    })

    $i(FormDeleteButtonId).click((obj : JQueryEventObject) => {
      Modal.Accept("Er du sikker?", "Er du sikker på at du vil slette siden?", () => {

      }, "Ja", "Nej. Ikke alligevel")
    })

  }

  private def bindEventHandlers() = {
    val updatePairs : List[(String,(Any, ViewPage) => ViewPage)] = List(
      FormNameId -> ((a,vp) => {vp.copy(name = a.toString)}),
      FormDescriptionId -> ((a,vp) => {vp.copy(description = Some(a.toString).filter(_.trim.size > 0))}),
      FormTagsId -> ((a,vp) => {vp.copy(tags = a.toString.split(";"))})
    )
    updatePairs.foreach {
      case (elmId, updator) => {
        $i(elmId).change((obj : JQueryEventObject) => {
          val value : Any = $i(elmId).value()
          updateWith(updator)(value)
        })
      }
    }

  }

  private def updateWith(func : (Any, ViewPage) => ViewPage)(a : Any) = {
    currentViewPage = currentViewPage.map(p => func(a,p))
  }

  object ItemsTable {
    val imageCol = ImageColumn[ViewItem]("image", "Billede", None, en => Some(Locations.Items.itemImage(en.itemId)))
    val nameCol = StringColumn[ViewItem]("name", "Navn",_.name)
    val captionCol = StringColumn[ViewItem]("caption", "Beskrivelse",_.caption)
    val registeredCol = DateTimeColumn[ViewItem]("registered", "Registreret", en => Some(en.registered.toDateTime))
    val costValueCol = DoubleColumn[ViewItem]("costvalue", "Købsværdi", en => en.costValue )
    val askPriceCol = DoubleColumn[ViewItem]("askprice", "Til salg for", en => en.askPrice)

    val columns = List(imageCol, nameCol, captionCol, registeredCol, costValueCol, askPriceCol)
  }

  def displayItems(items : Seq[ViewItem]) = {
    Modal.tabled("pages-item-modal", "Matchende genstande", ItemsTable.columns, items)
  }



  object PageConnector extends WSConnector {

    import io.circe.generic.auto._
    import io.circe.syntax._
    import io.circe.parser._
    import dk.lokallykke.client.Messages.Pages._

    override def onJson: Option[Json => Unit] = Some((js : Json) => {
      js.as[ToClient.ToClientMessage].foreach {
        case mess => {
          mess.pageShells.foreach(ps => setPageShells(ps))
          mess.tags.foreach(tags => setTags(tags))
          mess.page.foreach(p => makeViewPageSelection(p))
          mess.errorMessage.foreach(err => showError(err))
          println(s"Here I am with ze message")
          mess.items.foreach(items => displayItems(items))
        }
      }
    })

    def requestPage(pageId : Long) : Unit = {
      val mess = ToServer.ToServerMessage(ToServer.GetPage, pageId = Some(pageId))
      send(mess)
    }

    def executeFilter(filter : Seq[String]) : Unit = {
      val mess = ToServer.ToServerMessage(ToServer.ExecuteFilter, tags = Some(filter))
      send(mess)
    }

    def savePage(page : ViewPage) : Unit = {
      val mess = ToServer.ToServerMessage(ToServer.SavePage, Some(page))
      send(mess)
    }

    def deletePage(pageId : Long) : Unit = {
      val mess = ToServer.ToServerMessage(ToServer.DeletePage, pageId = Some(pageId))
      send(mess)
    }

    private def send(mess : ToServer.ToServerMessage) : Unit = {
      val str : String = mess.asJson.toString
      println(s"Sending message: $str")
      super.!(str)
    }


  }


}
