package com.codacy.client.stash

import org.joda.time.DateTime
import play.api.libs.json.{JodaReads, Reads}

object DateTimeImplicits {
  implicit val datetimeReads: Reads[DateTime] = JodaReads.DefaultJodaDateTimeReads
}
