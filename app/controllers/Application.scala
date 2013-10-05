package controllers

import model.Flight

import org.joda.time.DateTime

import play.api._
import play.api.mvc._

import riak.Riak
import riak.RiakMappings._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object Application extends Controller {

  def index = Action.async {
    val flight = Flight(1234, "KA", 789, DateTime.now(), DateTime.now().plusHours(7), 100, 70)
    Riak.save(flight)

    Riak.byId[Flight]("1234").map { flight =>
      Ok(flight.toString())
    }
  }

  def generate = Action.async {
    val t = System.currentTimeMillis()

    val rand = new Random()
    val codes = ('A' to 'Z').flatMap { first =>
      ('A' to 'Z').map { "" + first + _ }
    }
    val departure = DateTime.now().plusMinutes(rand.nextInt(36000))
    val capacity = rand.nextInt(400)
    val futures = codes.flatMap { code =>
      (1 to 9).map { i =>
        val id = (System.currentTimeMillis() * 10 + i) * 1000 + rand.nextInt(1000)
        Riak.save(Flight(
          id,
          code,
          rand.nextInt(10000).toShort,
          departure,
          departure.plusMinutes(rand.nextInt(36000)),
          capacity,
          rand.nextInt(capacity)
        ))
      }
    }

    Future.sequence(futures).map { list =>
      val d = System.currentTimeMillis() - t
      Ok("Duration: $d ms")
    }
  }

}