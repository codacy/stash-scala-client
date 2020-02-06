package com.codacy.client.stash.service

import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash.{SshKey, User, UserSshKey}
import play.api.libs.json.Json

class UserServices(client: StashClient) {

  /**
    * Gets the basic information associated with the authenticated account.
    */
  def getUsername: RequestResponse[String] = {
    client
      .doRequest[String]("/plugins/servlet/applinks/whoami", "GET", None)
      .fold(identity, {
        case (200, body) => RequestResponse(Option(body))
        case _ => RequestResponse(Option.empty, "", hasError = true)
      })
  }

  /**
    * Gets the basic information associated with the token owner account.
    */
  def getUsers: RequestResponse[Seq[User]] = {
    client.execute(Request("/rest/api/1.0/users", classOf[Seq[User]]))
  }

  /**
    * Gets the basic information associated with an account.
    */
  def getUser(username: String): RequestResponse[User] = {
    client.execute(Request(s"/rest/api/1.0/users/$username?avatarSize=64", classOf[User]))
  }

  /**
   * Gets the basic information associated with an account, including their avatarUrls.
   */
  def getUserWithAvatar(username: String, size: Option[Int]): RequestResponse[User] = {
    client.execute(Request(s"/rest/api/1.0/users/$username?avatarSize=${size.getOrElse(64)}", classOf[User]))
  }

  /**
    * Creates a ssh key
    */
  def createKey(projectKey: String, key: String, permission: String = "PROJECT_READ"): RequestResponse[SshKey] = {
    val url = s"/rest/keys/1.0/projects/$projectKey/ssh"

    val values = Json.obj("key" -> Json.obj("text" -> key), "permission" -> permission)

    client.postJson(Request(url, classOf[SshKey]), values)
  }

  /**
    * Add a new ssh key an authenticated user
    */
  def createUserKey(key: String): RequestResponse[UserSshKey] = {
    val url = "/rest/ssh/1.0/keys"

    val values = Json.obj("text" -> key)

    client.postJson(Request(url, classOf[UserSshKey]), values)
  }

  /**
    * Remove specific ssh keys from an authenticated user
    */
  def deleteUserKey(keyId: Long): RequestResponse[Boolean] = {
    val url = s"/rest/ssh/1.0/keys/$keyId"

    client.delete(url)
  }

}
