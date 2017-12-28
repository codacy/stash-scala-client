package com.codacy.client.stash.service

import com.codacy.client.stash.CommitComment
import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import play.api.libs.json.Json

class CommitServices(client: StashClient) {

  def createComment(projectKey: String, repo: String, commit: String, body: String): RequestResponse[CommitComment] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repo/commits/$commit/comments"

    val values = Json.obj(
      "text" -> body
      //      , "anchor" -> Json.obj(
      //        "line" -> 1,
      //        "lineType" -> "CONTEXT",
      //        "fileType" -> "FROM",
      //        "path" -> "path/to/file",
      //        "srcPath" -> "path/to/file"
      //      )
    )

    client.postJson(Request(url, classOf[CommitComment]), values)
  }

}
