package TravelCompanionScala {
package snippet {

import _root_.scala.xml.{NodeSeq, Text}

import _root_.net.liftweb._
import common.{Full, Box, Empty}
import http._
import S._
import util._
import Helpers._

import TravelCompanionScala.model._

/**
 * Created by IntelliJ IDEA.
 * User: Ralf Muri
 * Date: 09.04.2010
 * Time: 17:14:14
 * To change this template use File | Settings | File Templates.
 */


object TourEnum extends Enumeration {
  val OWN_TOURS = Value("OwnTours")
  val OTHERS_TOURS = Value("OthersTours")
}

// Set up a requestVar to track the TOUR object for edits and adds
object tourVar extends RequestVar[Tour](new Tour())

class TourSnippet {
  def tour = tourVar.is

  def doRemove() = {
    val t = Model.merge(tour)
    Model.remove(t)
    S.redirectTo("/tour/list")
  }

  def viewTour(html: NodeSeq): NodeSeq = {
    bind("tour", html, "name" -> tour.name, "description" -> tour.description)
  }

  def editTour(html: NodeSeq): NodeSeq = {
    def doEdit() = {
      Model.mergeAndFlush(tour)
      S.redirectTo("/tour/list")
    }

    val currentTour = tour

    if (currentTour.owner == null) {
      currentTour.owner = UserManagement.currentUser
    }

    bind("tour", html,
      "name" -> SHtml.text(currentTour.name, currentTour.name = _),
      "description" -> SHtml.textarea(currentTour.description, currentTour.description = _),
      "owner" -> SHtml.text(currentTour.owner.name, currentTour.owner.name = _),
      "submit" -> SHtml.submit(?("save"), () => {tourVar(currentTour); doEdit}))
  }


  def listTours(html: NodeSeq): NodeSeq = {
    val which = S.attr("which").map(_.toString) openOr "AllTours"
    tours(TourEnum.withName(which)).flatMap(tour => bind("tour", html,
      "name" -> SHtml.link("view", () => tourVar(tour), Text(tour.name)),
      "description" -> tour.description,
      "creator" -> tour.owner.name,
      "addStage" -> SHtml.link("stage/edit", () => tourVar(tour), Text(?("tour.addStage"))),
      "edit" -> SHtml.link("edit", () => tourVar(tour), Text(?("edit"))),
      "view" -> SHtml.link("view", () => tourVar(tour), Text(?("view"))),
      "remove" -> SHtml.link("remove", () => {tourVar(tour); doRemove}, Text(?("remove")))))
  }

  private def tours(which: TourEnum.Value): List[Tour] = {
    which match {
      case TourEnum.OWN_TOURS => Model.createNamedQuery[Tour]("findTourByOwner").setParams("id" -> UserManagement.currentUser.id).findAll.toList
      case TourEnum.OTHERS_TOURS => Model.createNamedQuery[Tour]("findTourByOthers").setParams("id" -> UserManagement.currentUser.id).findAll.toList
    }
  }
}
}
}