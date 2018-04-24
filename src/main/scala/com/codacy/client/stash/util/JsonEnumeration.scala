package com.codacy.client.stash.util

import play.api.libs.json.Format

trait JsonEnumeration extends Enumeration {
  self: Enumeration =>

  implicit lazy val format = Format(Implicits.enumReads(self), Implicits.enumWrites)

  def findByName(name: String): Option[Value] = {
    values.find(v => v.toString.toLowerCase == name.toLowerCase)
  }
}
