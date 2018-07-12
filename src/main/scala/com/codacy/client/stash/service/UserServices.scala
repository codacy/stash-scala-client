package com.codacy.client.stash.service

import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash.{SshKey, SshKeyByUSer, User}
import play.api.libs.json.Json

class UserServices(client: StashClient) {

  /*
   * Gets the basic information associated with the token owner account.
   */
  def getUsers: RequestResponse[Seq[User]] = {
    client.execute(Request("/rest/api/1.0/users", classOf[Seq[User]]))
  }

  /*
   * Gets the basic information associated with an account.
   */
  def getUser(username: String): RequestResponse[User] = {
    client.execute(Request(s"/rest/api/1.0/users/$username", classOf[User]))
  }

  /*
   * Creates a ssh key
   */
  def createKey(projectKey: String, key: String, permission: String = "PROJECT_READ"): RequestResponse[SshKey] = {
    val url = s"/rest/keys/1.0/projects/$projectKey/ssh"

    val values = Json.obj(
      "key" -> Json.obj(
        "text" -> key
      ),
      "permission" -> permission
    )

    client.postJson(Request(url, classOf[SshKey]), values)
  }

  /*
   * Add a new ssh key an authenticated user
   */
  def createUserKey(key: String): RequestResponse[SshKeyByUSer] =  {
    val url = s"/rest/ssh/1.0/keys"

    val values = Json.obj(
      "text" -> key
    )

    client.postJson(Request(url, classOf[SshKeyByUSer]), values)
  }

}
