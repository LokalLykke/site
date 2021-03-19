package dk.lokallykke.client.viewmodel.pages

import dk.lokallykke.client.util.editor.Editor.EditorData.Block

case class ViewPage(
                   pageId : Long,
                   name : String,
                   description : Option[String],
                   tags : Seq[String],
                   blocks : Seq[Block]
                   )
