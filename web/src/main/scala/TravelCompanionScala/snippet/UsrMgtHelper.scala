package TravelCompanionScala.snippet

import xml.NodeSeq
import TravelCompanionScala.model.UserManagement
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import scala.collection.JavaConversions._


/**
 * Created by IntelliJ IDEA.
 * User: rmuri
 * Date: 13.04.2010
 * Time: 12:08:13
 * To change this template use File | Settings | File Templates.
 */

class UsrMgtHelper {
  def showIf(html: NodeSeq, cond: Boolean): NodeSeq = {
    if (cond)
      html
    else
      NodeSeq.Empty
  }

  def showIfAuthenticated(html: NodeSeq): NodeSeq = {
    showIf(html, UserManagement.loggedIn_?)

  }

  def showIfTourOwner(html: NodeSeq): NodeSeq = {
    showIf(html, tourVar.is.owner == UserManagement.currentUser)
  }

  def showIfBlogEntryOwner(html: NodeSeq): NodeSeq = {
    showIf(html, blogEntryVar.is.owner == UserManagement.currentUser)
  }

  def showIfInRole(html: NodeSeq): NodeSeq = {
    val role = S.attr("role").map(_.toString) openOr "Guest"
    showIf(html, UserManagement.currentUser.roles.exists(_.name == role))
  }

  def currentUser(html: NodeSeq): NodeSeq = {
    bind("user", html, "name" -> UserManagement.currentUser.name)
  }
}