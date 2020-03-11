package com.codacy.client.stash.service

import com.codacy.client.stash.WebHook
import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import play.api.libs.json.{JsArray, JsString, Json}

class WebhooksServices(client: StashClient) {

  def list(projectKey: String, repositorySlug: String): RequestResponse[Seq[WebHook]] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repositorySlug/webhooks"

    client.executePaginated(Request(url, classOf[Seq[WebHook]]))()
  }

  def create(
      projectKey: String,
      repositorySlug: String,
      name: String,
      hookCallbakUrl: String,
      events: Set[String],
      active: Boolean
  ): RequestResponse[WebHook] = {
    val url = s"/rest/api/latest/projects/$projectKey/repos/$repositorySlug/webhooks"

    val values = Json.obj(
      "name" -> name,
      "url" -> hookCallbakUrl,
      "events" -> JsArray(events.map(JsString)(collection.breakOut)),
      "configuration" -> Json.obj(),
      "active" -> active
    )

    client.postJson(Request(url, classOf[WebHook]), values)
  }

  def update(
      projectKey: String,
      repositorySlug: String,
      webhookId: Long,
      name: String,
      hookCallbakUrl: String,
      events: Set[String],
      active: Boolean
  ): RequestResponse[WebHook] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repositorySlug/webhooks/$webhookId"

    val values = Json.obj(
      "name" -> name,
      "url" -> hookCallbakUrl,
      "events" -> JsArray(events.map(JsString)(collection.breakOut)),
      "configuration" -> Json.obj(),
      "active" -> active
    )

    client.putJson(Request(url, classOf[WebHook]), values)
  }

  def delete(projectKey: String, repositorySlug: String, webhookId: Long): RequestResponse[Boolean] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repositorySlug/webhooks/$webhookId"

    client.delete(url)()
  }

}
