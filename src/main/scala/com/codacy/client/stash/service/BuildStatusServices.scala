package com.codacy.client.stash.service

import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash.{BuildStatus, TimestampedBuildStatus}

import play.api.libs.json.{JsValue, Json}

class BuildStatusServices(client: StashClient) {

  /*
   * Gets the build status for the given commit.
   */
  def getBuildStatus(commit: String): RequestResponse[Seq[TimestampedBuildStatus]] = {
    client.executePaginated(Request(urlPath(commit), classOf[Seq[TimestampedBuildStatus]]))
  }

  /*
   * Creates a build status for the given commit.
   */
  def createBuildStatus(commit: String, buildStatus: BuildStatus): RequestResponse[String] = {
    client.postJson(Request(urlPath(commit), classOf[String]), statusPayload(buildStatus))
  }

  /*
   * Update the build status for the given commit.
   */
  def updateBuildStatus(owner: String, repository: String, commit: String, buildStatus: BuildStatus): RequestResponse[BuildStatus] = {
    client.postJson(Request(urlPath(commit), classOf[BuildStatus]), statusPayload(buildStatus))
  }

  private def urlPath(commit: String): String = s"/rest/build-status/1.0/commits/$commit"

  private def statusPayload(buildStatus: BuildStatus): JsValue = {
    Json.obj(
      "state" -> buildStatus.state.toString,
      "key" -> buildStatus.key,
      "name" -> buildStatus.name,
      "url" -> buildStatus.url,
      "description" -> buildStatus.description
    )
  }
}
