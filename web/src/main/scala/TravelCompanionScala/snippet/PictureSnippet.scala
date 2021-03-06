package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common._
import http._
import S._
import util._
import Helpers._
import net.liftweb.imaging._

import TravelCompanionScala.model._
import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, InputStream}
import javax.imageio.ImageIO
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 26.04.2010
 * Time: 11:06:04
 * To change this template use File | Settings | File Templates.
 */

object pictureVar extends RequestVar[Picture](new Picture)

class PictureSnippet {
  def doRemove(picture: Picture) {
    val p = Model.merge(picture)
    Model.remove(p)
    S.redirectTo("/picture/list")
  }

  var fileHolder: Box[FileParamHolder] = Empty

  def addPicture(html: NodeSeq): NodeSeq = {
    val currentPicture = new Picture

    def createThumbnail(data: Array[Byte]): Array[Byte] = {
      val in: InputStream = new ByteArrayInputStream(data)
      val original: BufferedImage = ImageIO.read(in)
      val thumbnail: BufferedImage = ImageResizer.square(None, original, 125)
      val baos: ByteArrayOutputStream = new ByteArrayOutputStream()
      ImageIO.write(thumbnail, "jpg", baos)
      baos.toByteArray
    }

    def doSave(picture: Picture) {
      picture.owner = UserManagement.currentUser
      fileHolder match {
        case Full(FileParamHolder(_, mime: String, _, data))
          if (mime.startsWith("image/")) => {
          picture.thumbnail = createThumbnail(data)
          picture.image = data
          picture.imageType = mime
        }
        case Full(_) => {
          S.error("Invalid Attachment")
          S.redirectTo("/picture/create")
        }
        case Empty => {
          S.error("Invalid Attachment")
          S.redirectTo("/picture/create")
        }
        case Failure(_, _, _) => {
          S.error("Invalid Attachment")
          S.redirectTo("/picture/create")
        }
      }
      if (validator.is_valid_entity_?(picture)) {
        Model.mergeAndFlush(picture)
        S.redirectTo("/picture/list")
      }
    }

    val tours = Model.createNamedQuery[Tour]("findTourByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    val tchoices = List("" -> ("- " + S.?("none") + " -")) ::: tours.map(tour => (tour.id.toString -> tour.name)).toList

    val entries = Model.createNamedQuery[BlogEntry]("findEntriesByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    val echoices = List("" -> ("- " + S.?("none") + " -")) ::: entries.map(entry => (entry.id.toString -> entry.title)).toList

    bind("picture", html,
      "name" -%> SHtml.text(currentPicture.name, currentPicture.name = _),
      "file" -%> SHtml.fileUpload(fh => fileHolder = Full(fh)),
      "description" -%> SHtml.textarea(currentPicture.description, currentPicture.description = _),
      "tour" -> SHtml.select(tchoices, Empty, (tourId: String) => {if (tourId != "") currentPicture.tour = Model.getReference(classOf[Tour], tourId.toLong) else currentPicture.tour = null}),
      "blogEntry" -> SHtml.select(echoices, Empty, (entryId: String) => {if (entryId != "") currentPicture.blogEntry = Model.getReference(classOf[BlogEntry], entryId.toLong) else currentPicture.blogEntry = null}),
      "submit" -%> SHtml.submit(?("save"), () => doSave(currentPicture)))
  }

  def showPicturesFromTour(html: NodeSeq): NodeSeq = {
    val currentTour = tourVar.is
    val pictures = Model.createNamedQuery[Picture]("findPicturesByTour").setParams("tour" -> currentTour).findAll.toList
    listPictures(html, pictures)
  }

  def showPicture(html: NodeSeq): NodeSeq = {
    listPictures(html, List(pictureVar.is))
  }

  def listPictures(html: NodeSeq, pictures: List[Picture]): NodeSeq = {
    pictures.flatMap(picture => bind("picture", html,
      "thumbnail" -> SHtml.link("/picture/view", () => pictureVar(picture), <img src={"/image/thumbnail/" + picture.id}/>),
      "image" -%> <img src={"/image/full/" + picture.id}/>,
      "name" -> picture.name,
      "description" -> picture.description,
      "owner" -> picture.owner.name,
      "belongsTo" -> {
        var n: NodeSeq = NodeSeq.Empty
        if (picture.tour != null)
          n = n ++ bind("link", chooseTemplate("choose", "belongsTo", html),
            "title" -> Text(S.?("tour")),
            "link" -> SHtml.link("/tour/view", () => tourVar(picture.tour), Text(picture.tour.name)))
        if (picture.blogEntry != null)
          n = n ++ bind("link", chooseTemplate("choose", "belongsTo", html),
            "title" -> Text(S.?("blog.entry")),
            "link" -> SHtml.link("/blog/view", () => blogEntryVar(picture.blogEntry), Text(picture.blogEntry.title)))
        n
      },
      "remove" -> SHtml.link("remove", () => doRemove(picture), Text(?("remove")))))
  }

  def listOtherPictures(html: NodeSeq): NodeSeq = {
    val pictures = Model.createNamedQuery[Picture]("findPicturesByOthers").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listPictures(html, pictures)
  }

  def listOwnPictures(html: NodeSeq): NodeSeq = {
    val pictures = Model.createNamedQuery[Picture]("findPicturesByOwner").setParams("owner" -> UserManagement.currentUser).findAll.toList
    listPictures(html, pictures)
  }
}