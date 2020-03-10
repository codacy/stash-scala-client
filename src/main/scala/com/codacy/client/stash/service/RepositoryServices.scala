package com.codacy.client.stash.service

import com.codacy.client.stash.client.{PageRequest, Request, RequestResponse, StashClient}
import com.codacy.client.stash.{Group, Repository, SshKeySimple, UserPermission}
import play.api.libs.json.Json

class RepositoryServices(client: StashClient) {

  val BASE: String = "/rest/api/1.0/projects"

  /**
    * Gets the list of the user's repositories. Private repositories only appear on this list
    * if the caller is authenticated and is authorized to view the repository.
    */
  def getRepositories(projectKey: String, pageRequest: Option[PageRequest]): RequestResponse[Seq[Repository]] =
    pageRequest match {
      case Some(pageRequest) =>
        client.executePaginatedWithPageRequest(
          Request(s"$BASE/$projectKey/repos", classOf[Seq[Repository]]),
          pageRequest = pageRequest
        )()
      case None => client.executePaginated(Request(s"$BASE/$projectKey/repos", classOf[Seq[Repository]]))()
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
    * Retrieve a page of repositories based on query parameters that control the search.
    *
    * @param projectName This will limit the resulting repository list to ones whose project's name matches this parameter's value
    *                    The match will be done case-insensitive and any leading and/or trailing whitespace characters on the projectname parameter will be stripped.
    *                    Note that this filed IS the `Project Name` NOT the `Project Key`
    *                    Ex: TesT will return results for TEST, TEST2, etc
    *
    * @param repositoryName This will limit the resulting repository list to ones whose name matches this parameter's value
    *                       The match will be done case-insensitive and any leading and/or trailing whitespace characters on the repositoryName parameter will be stripped.
    *                       Ex: Codacy will return codacy-website, codacy-worker, etc
    *
    * @param pageRequest If specified, will return results only for the requested page
    *
    * @param permission  If specified, it must be a valid repository permission level name and will limit the resulting repository list
    *                    to ones that the requesting user has the specified permission level to.
    *                    The currently supported explicit permission values are REPO_READ, REPO_WRITE and REPO_ADMIN.
    */
  def search(
      projectName: String,
      repositoryName: String,
      pageRequest: Option[PageRequest],
      permission: Option[String] = None
  ): RequestResponse[Seq[Repository]] = {
    val baseParameters = Map("projectname" -> projectName, "name" -> repositoryName)

    val parameters = permission.fold(baseParameters) { permission =>
      baseParameters + ("permission" -> permission)
    }

    val request: Request[Seq[Repository]] = Request(s"/rest/api/1.0/repos", classOf[Seq[Repository]])

    pageRequest match {
      case Some(pageRequest) =>
        client.executePaginatedWithPageRequest(request, pageRequest)(parameters)
      case _ =>
        client.executePaginated(request)(parameters)
    }
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified repository.
    *
    * The authenticated user must have REPO_ADMIN permission for the specified repository or a higher project or global permission to call this resource.
    */
  def getRepositoryUsers(
      projectKey: String,
      repositorySlug: String,
      pageRequest: Option[PageRequest]
  ): RequestResponse[Seq[UserPermission]] = pageRequest match {
    case Some(pageRequest) =>
      client.executePaginatedWithPageRequest(
        Request(s"$BASE/$projectKey/repos/$repositorySlug/permissions/users", classOf[Seq[UserPermission]]),
        pageRequest = pageRequest
      )()
    case None =>
      client.executePaginated(
        Request(s"$BASE/$projectKey/repos/$repositorySlug/permissions/users", classOf[Seq[UserPermission]])
      )()
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified repository.
    *
    * The authenticated user must have REPO_ADMIN permission for the specified repository or a higher project or global permission to call this resource.
    */
  def getRepositoryUserPermissions(
      projectKey: String,
      repositorySlug: String,
      user: String,
      pageRequest: Option[PageRequest]
  ): RequestResponse[Seq[UserPermission]] = {
    val filterParam = Map("filter" -> user)
    pageRequest match {
      case Some(pageRequest) =>
        client.executePaginatedWithPageRequest(
          Request(
            s"$BASE/$projectKey/repos/$repositorySlug/permissions/users?filter=$user",
            classOf[Seq[UserPermission]]
          ),
          pageRequest = pageRequest
        )(params = filterParam)
      case None =>
        client.executePaginated(
          Request(
            s"$BASE/$projectKey/repos/$repositorySlug/permissions/users?filter=$user",
            classOf[Seq[UserPermission]]
          )
        )(params = filterParam)
    }
  }

  /**
    * Retrieve a page of groups that have been granted at least one permission for the specified repository.
    *
    * The authenticated user must have REPO_ADMIN permission for the specified repository or a higher project or global permission to call this resource.
    */
  def getRepositoryGroups(
      projectKey: String,
      repositorySlug: String,
      pageRequest: Option[PageRequest]
  ): RequestResponse[Seq[Group]] = pageRequest match {
    case Some(pageRequest) =>
      client.executePaginatedWithPageRequest(
        Request(s"$BASE/$projectKey/repos/$repositorySlug/permissions/groups", classOf[Seq[Group]]),
        pageRequest
      )()
    case None =>
      client.executePaginated(
        Request(s"$BASE/$projectKey/repos/$repositorySlug/permissions/groups", classOf[Seq[Group]])
      )()
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
    val url = s"/rest/keys/1.0/projects/$projectKey/repos/$repo/ssh"
    val values = Json.obj("key" -> Json.obj("text" -> key), "permission" -> permission)

    client.postJson(Request(url, classOf[SshKeySimple]), values)
  }

}
