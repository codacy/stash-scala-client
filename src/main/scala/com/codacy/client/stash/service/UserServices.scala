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
      .doRequest[String]("/plugins/servlet/applinks/whoami", "GET", payload = None)
      .fold(identity, {
        case (200, body) => RequestResponse(Option(body))
        case _ => RequestResponse(value = Option.empty, hasError = true)
      })
  }

  /**
    * Gets the basic information associated with the token owner account with a optional filter.
    */
  def getUsers(filter: Option[String]): RequestResponse[Seq[User]] = {
    val baseUrl = "/rest/api/1.0/users"
    val url = filter.fold(baseUrl)(f => s"$baseUrl?name=$f")
    client.execute(Request(url, classOf[Seq[User]]))
  }

  /**
    * Gets the basic information associated with an account.
    */
  def getUser(username: String): RequestResponse[User] = {
    client.execute(Request(s"/rest/api/1.0/users/$username", classOf[User]))().value.orElse {
      getUsers(Option(username)).value.flatMap(_.collectFirst { case user if user.username == username => user })
    }.fold(RequestResponse[User](None))(user => RequestResponse[User](Option(user)))
  }

  /**
    * Gets the basic information associated with an account, including their avatarUrls.
    */
  def getUserWithAvatar(username: String, size: Option[Int]): RequestResponse[User] = {
    client.execute(Request(s"/rest/api/1.0/users/$username", classOf[User]))(Map("avatarSize" -> size.getOrElse(64).toString)).value.orElse {
      getUsers(Option(username)).value.flatMap(_.collectFirst { case user if user.username == username => user })
    }.fold(RequestResponse[User](None))(user => RequestResponse[User](Option(user)))
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

    client.delete(url)()
  }

}
