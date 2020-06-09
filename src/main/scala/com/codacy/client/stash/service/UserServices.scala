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
  def getUsers(name: Option[String] = None, params: Map[String, String] = Map.empty): RequestResponse[Seq[User]] = {
    val extraParams = name.map("filter" -> _).toMap
    val baseUrl = "/rest/api/1.0/users"
    client.executePaginated(Request(baseUrl, classOf[Seq[User]]))(params ++ extraParams)
  }

  /**
    * Gets the basic information associated with an account.
    */
  def getUser(username: String): RequestResponse[User] = {
    val response = client.execute(Request(s"/rest/api/1.0/users/$username", classOf[User]))()
    getUserFallback(username, Map.empty, response)
  }

  /**
    * Gets the basic information associated with an account, including their avatarUrls.
    */
  def getUserWithAvatar(username: String, size: Option[Int]): RequestResponse[User] = {
    val params = Map("avatarSize" -> size.getOrElse(64).toString)
    val response = client.execute(Request(s"/rest/api/1.0/users/$username", classOf[User]))(params)
    getUserFallback(username, params, response)
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

  /**
    *
    * We need this fallback for the cases when a username contains special characters because the
    * getUser method don't find them and the only way is passing the name as a filter on getUsers method
    *
    */
  private def getUserFallback(
      username: String,
      params: Map[String, String],
      userResponse: RequestResponse[User]
  ): RequestResponse[User] = {
    userResponse.value
      .orElse {
        // Since we are using a filter as query parameter this can bring more users and we need to find the one using the username passed by parameter as name
        getUsers(Option(username), params).value.flatMap(_.find(_.name == username))
      }
      .fold(RequestResponse[User](None))(user => RequestResponse[User](Option(user)))
  }

}
