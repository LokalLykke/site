package lokallykke.instagram

import lokallykke.LocallykkeConfig
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import lokallykke.helpers.Extensions._
import play.api.libs.json.{JsArray, JsObject, Json}

import java.io.File
import java.sql.Timestamp
import scala.io.Source
import scala.sys.process._

object InstagramLoader {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val OutputDir = new File("download/instaloader")
  private val ResultDir = new File(OutputDir, "lokallykke")
  private val instagramUser = LocallykkeConfig.Instagram.user
  private val instagramPassword = LocallykkeConfig.Instagram.password



  def downloadItems(implicit loaderObserver : LoaderObserver = LoaderObserver.Sink) = {
    /*if(ResultDir.exists())
      ResultDir.delete()
    val cmd = Process(Seq(
    """c:\tools\instaloader\instaloader.exe""",
      "--stories",
      "--no-compress-json",
      s"--login=$instagramUser",
      s"--password=$instagramPassword",
      s"$instagramUser"),
      OutputDir)
    loaderObserver.onProgressChange(LoaderObserver.ParsingState.Downloading)
    val res = cmd.!!
    loaderObserver.onCommandLineUpdate(res)
    logger.info(s"Result from InstagramLoader:")
    logger.info(res)*/
    val parsed = parseResponse
    loaderObserver.onProgressChange(LoaderObserver.ParsingState.Done)
    logger.info(s"Downloaded and parsed ${parsed.size} items from Instagram")
    parsed
  }

  object ParseObjects {
    case class ParseFile(baseName : String, fileType : String, file : File, bytes : Array[Byte], indx : Option[Int])
  }


  def parseResponse(implicit loaderObserver : LoaderObserver = LoaderObserver.Sink) = {
    loaderObserver.onProgressChange(LoaderObserver.ParsingState.Parsing)
    val read = readFilesIn(ResultDir)
    (read.groupBy(_.baseName).toList.map {
      case (nam, ents) => {
        val fileCaption = ents.find(_.fileType ~ "txt").map {
          case ent => Source.fromBytes(ent.bytes, "UTF-8").toList.mkString("")
        }
        (ents.find(_.fileType ~ "json"), ents.filter(_.fileType ~~ Seq("jpg", "png"))) match {
          case (None, _) => None
          case (_,seq) if seq.isEmpty => None
          case (Some(jsEnt), imgs) => {
            parseJson(jsEnt.bytes) match {
              case None => None
              case Some((id, width, height, caption, timestamp)) => {
                val capTags = caption.orElse(fileCaption).map(c => parseCaptionWithTags(c))
                val (capOpt : Option[String], tags : Seq[String]) = (capTags.map(_._1), capTags.map(_._2).getOrElse(Nil))
                val headImg = imgs.head
                Some(InstagramItem(id, headImg.bytes, width, height, capOpt, new Timestamp(timestamp * 1000L), headImg.fileType, tags))
              }
            }
          }
        }
      }
    }).collect {
      case Some(it) => it
    }
  }

  private val indexRegex = ".+_([0-9]{1,2})".r
  def readFilesIn(folder : File) : Seq[ParseObjects.ParseFile] = {
    folder.listFiles().filter(_.getName.contains(""".""")).map {
      case file => {
        val byts = FileUtils.readFileToByteArray(file)
        val (fullBaseName, fileType) = file.getName.splitOnLastOccurrenceOf('.').get
        val (baseName, fileIndx) = fullBaseName match {
          case indexRegex(indx) => (fullBaseName.substring(0, fullBaseName.length - (indx.length + 1)), Some(indx.toInt))
          case _ => (fullBaseName, None)
        }
        ParseObjects.ParseFile(baseName, fileType, file, byts, fileIndx)
      }
    }
  }

  private def parseJson(byts : Array[Byte]) = {
    val json = Json.parse(new String(byts, "utf-8"))
    json match {
      case obj : JsObject => {
        val node = obj.value("node")
        val height = (node \ "dimensions" \ "height").as[Int]
        val width = (node \ "dimensions" \ "width").as[Int]
        val id = (node \ "id").as[String]
        val caption = (node \ "edge_media_to_caption" \ "edges") match {
          case arr : JsArray => Some(arr.value.map(_ \ "node" \ "text").map(_.as[String]).mkString(""))
          case _ => None
        }
        val timestamp = (node \ "taken_at_timestamp").as[Long]
        Some((id, width, height, caption, timestamp))
      }
      case _ => None
    }
  }

  private val tagsRegex = "#(\\w|\\-)+".r

  def parseCaptionWithTags(str : String) : (String, Seq[String]) = {
    tagsRegex.findAllIn(str).toList.foldLeft(("", Seq.empty[String])) {
      case ((ret : String, tags : Seq[String]) ,tag : String) => (ret.replaceAll(tag,""),  tags :+ tag.replaceAll("#",""))
    }
  }


  private def isWindows : Boolean = {
    println(System.getProperty("os.name")).toString.toLowerCase.contains("win")
  }




}
