package com.codacy.client.stash.client

final case class PageRequest(start: Option[String], limit: Option[Int]) {

  def getStart: Int = start.fold(0)(_.toInt)

  def getLimit: Int = limit.getOrElse(25)

}
