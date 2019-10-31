package com.codacy.client.stash

import com.codacy.client.stash.util.JsonEnumeration
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}

object CommitStatus extends JsonEnumeration {
  val InProgress = Value("INPROGRESS")
  val Successful = Value("SUCCESSFUL")
  val Failed = Value("FAILED")
}

case class BuildStatus(state: CommitStatus.Value, key: String, name: String, url: String, description: String)

object BuildStatus {
  implicit val fmt = Json.format[BuildStatus]
}

case class TimestampedBuildStatus(
    state: CommitStatus.Value,
    key: String,
    name: String,
    url: String,
    description: String,
    dateAdded: DateTime
)

object TimestampedBuildStatus {
  implicit val datetimeReader: Reads[DateTime] = Reads.DefaultJodaDateReads
  implicit val fmt = Json.format[TimestampedBuildStatus]
}
