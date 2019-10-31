package com.codacy.client.stash.service

import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash.{Repository, SshKeySimple}
import play.api.libs.json.Json

class RepositoryServices(client: StashClient) {

  /*
   * Gets the list of the user's repositories. Private repositories only appear on this list
   * if the caller is authenticated and is authorized to view the repository.
   */
  def getRepositories(projectKey: String): RequestResponse[Seq[Repository]] = {
    client.executePaginated(Request(s"/rest/api/1.0/projects/$projectKey/repos", classOf[Seq[Repository]]))
  }

  /*
   * Creates a ssh key
   */
  def createKey(
      projectKey: String,
      repo: String,
      key: String,
      permission: String = "REPO_READ"
  ): RequestResponse[SshKeySimple] = {
    val url = s"/rest/keys/1.0/projects/$projectKey/repos/$repo/ssh"

    val values = Json.obj("key" -> Json.obj("text" -> key), "permission" -> permission)

    client.postJson(Request(url, classOf[SshKeySimple]), values)
  }

}
