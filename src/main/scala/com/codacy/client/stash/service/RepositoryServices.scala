package com.codacy.client.stash.service

import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash.{Permission, Repository, SshKeySimple, User}
import play.api.libs.json.Json

class RepositoryServices(client: StashClient) {

  val BASE: String = "/rest/api/1.0/projects"

  /**
    * Gets the list of the user's repositories. Private repositories only appear on this list
    * if the caller is authenticated and is authorized to view the repository.
    */
  def getRepositories(projectKey: String): RequestResponse[Seq[Repository]] = {
    client.executePaginated(Request(s"$BASE/$projectKey/repos", classOf[Seq[Repository]]))
  }

  /**
    * Retrieve the repository matching the supplied projectKey and repositorySlug.
    *
    * The authenticated user must have REPO_READ permission for the specified repository to call this resource.
    */
  def getRepository(projectKey: String, repositorySlug: String): RequestResponse[Repository] = {
    client.execute(Request(s"$BASE/$projectKey/repos/$repositorySlug", classOf[Repository]))
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified repository.
    *
    * The authenticated user must have REPO_ADMIN permission for the specified repository or a higher project or global permission to call this resource.
    */
  def getRepositoryUsers(projectKey: String, repositorySlug: String): RequestResponse[Seq[User]] = {
    client.executePaginated(Request(s"$BASE/$projectKey/repos/$repositorySlug", classOf[Seq[User]]))
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified repository.
    *
    * The authenticated user must have REPO_ADMIN permission for the specified repository or a higher project or global permission to call this resource.
    */
  def getRepositoryUserPermissions(
      projectKey: String,
      repositorySlug: String,
      user: String
  ): RequestResponse[Seq[Permission]] = {
    client.executePaginated(
      Request(s"$BASE/$projectKey/repos/$repositorySlug/permissions/users?filter=$user", classOf[Seq[Permission]])
    )
  }

  /**
    * Creates a ssh key
    */
  def createKey(
      projectKey: String,
      repo: String,
      key: String,
      permission: String = "REPO_READ"
  ): RequestResponse[SshKeySimple] = {
    val url = s"$BASE/$projectKey/repos/$repo/ssh"

    val values = Json.obj("key" -> Json.obj("text" -> key), "permission" -> permission)

    client.postJson(Request(url, classOf[SshKeySimple]), values)
  }

}
