package com.codacy.client.stash

import com.codacy.client.stash.util.JsonEnumeration
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json, Reads}

object CommitStatus extends JsonEnumeration {
  val InProgress: Value = Value("INPROGRESS")
  val Successful: Value = Value("SUCCESSFUL")
  val Failed: Value = Value("FAILED")
}

final case class BuildStatus(state: CommitStatus.Value, key: String, name: String, url: String, description: String)

object BuildStatus {
  implicit val fmt: Format[BuildStatus] = Json.format[BuildStatus]
}

final case class TimestampedBuildStatus(
    state: CommitStatus.Value,
    key: String,
    name: String,
    url: String,
    description: String,
    dateAdded: DateTime
)

object TimestampedBuildStatus {
  import DateTimeImplicits.datetimeReads
  implicit val fmt: Reads[TimestampedBuildStatus] = Json.reads[TimestampedBuildStatus]
}
