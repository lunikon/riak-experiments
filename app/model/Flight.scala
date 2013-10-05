package model

import org.joda.time.DateTime

case class Flight(
  id: Long,
  code: String,
  number: Short,
  departure: DateTime,
  arrival: DateTime,
  capacity: Int,
  load: Int)