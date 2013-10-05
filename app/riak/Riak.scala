package riak

import model.Flight

import org.joda.time.DateTime

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws._

import scala.concurrent.Future

trait RiakMapping[T] {
  val bucket: String
  val format: Format[T]
  def toId(obj: T): String
}

object RiakMappings {

  implicit val flightMapping = new RiakMapping[Flight] {
    val bucket = "flights"

    val format = (
      (__ \ "id").format[Long] and
      (__ \ "code").format[String] and
      (__ \ "number").format[Short] and
      (__ \ "departure").format[DateTime] and
      (__ \ "arrival").format[DateTime] and
      (__ \ "capacity").format[Int] and
      (__ \ "load").format[Int]
    )(Flight, unlift(Flight.unapply))

    def toId(obj: Flight) = obj.id.toString()
  }

}

object Riak {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def save[T](obj: T)(implicit mapping: RiakMapping[T]) = {
    val id = mapping.toId(obj)
    val data = Json.toJson(obj)(mapping.format)
    WS
      .url(s"http://127.0.0.1:10018/riak/${mapping.bucket}/$id")
      .put(data)
      .map(response => response.status)
  }

  def byId[T](id: String)(implicit mapping: RiakMapping[T]): Future[Option[T]] = {
    WS.url(s"http://127.0.0.1:10018/riak/${mapping.bucket}/$id").get().map { response =>
      response.status match {
        case 404 => None
        case _ => response.json.validate[T](mapping.format).fold(
          valid = (obj => Some(obj)),
          invalid = { err =>
            println(err)
            None
          }
        )
      }
    }
  }

}