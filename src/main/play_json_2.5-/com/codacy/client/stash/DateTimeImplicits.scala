package com.codacy.client.stash

import org.joda.time.DateTime
import play.api.libs.json.Reads

object DateTimeImplicits {
  implicit val datetimeReads: Reads[DateTime] = Reads.DefaultJodaDateReads
}
