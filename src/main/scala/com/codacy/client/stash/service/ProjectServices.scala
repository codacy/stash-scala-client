package com.codacy.client.stash.service

import com.codacy.client.stash.client.{PageRequest, Request, RequestResponse, StashClient}
import com.codacy.client.stash._
import com.codacy.client.stash.util.AvatarUtil

class ProjectServices(client: StashClient) {

  val BASE: String = "/rest/api/1.0/projects"

  /**
    * Only projects for which the authenticated user has the PROJECT_VIEW permission will be returned.
    */
  def findById(projectKey: String, includeAvatar: Boolean = false): RequestResponse[Project] = {
    val params = AvatarUtil.addAvatarToParams(includeAvatar)

    client.execute(Request(s"$BASE/$projectKey", classOf[Project]))(params)
  }

  /**
    * Only projects for which the authenticated user has the PROJECT_VIEW permission will be returned.
    */
  def findAll(pageRequest: Option[PageRequest], includeAvatar: Boolean = false): RequestResponse[Seq[Project]] = {
    val params = AvatarUtil.addAvatarToParams(includeAvatar)

    pageRequest match {
      case Some(pageRequest) =>
        client.executePaginatedWithPageRequest(Request(BASE, classOf[Seq[Project]]), pageRequest = pageRequest)(params)
      case None => client.executePaginated(Request(BASE, classOf[Seq[Project]]))(params)
    }
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified project.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission to call this resource.
    */
  def findUserPermissions(
      projectKey: String,
      user: String,
      pageRequest: Option[PageRequest]
  ): RequestResponse[Seq[UserPermission]] = {
    val parameters = Map("filter" -> user)

    pageRequest match {
      case Some(pageRequest) =>
        client.executePaginatedWithPageRequest(
          Request(s"$BASE/$projectKey/permissions/users", classOf[Seq[UserPermission]]),
          pageRequest = pageRequest
        )(params = parameters)
      case None =>
        client.executePaginated(Request(s"$BASE/$projectKey/permissions/users", classOf[Seq[UserPermission]]))(
          params = parameters
        )
    }
  }

  /**
    * Retrieve repositories from the project corresponding to the supplied projectKey.
    *
    * The authenticated user must have REPO_READ permission for the specified project to call this resource.
    */
  def findAllRepositories(projectKey: String, pageRequest: Option[PageRequest]): RequestResponse[Seq[Repository]] =
    pageRequest match {
      case Some(pageRequest) =>
        client
          .executePaginatedWithPageRequest(
            Request(s"$BASE/$projectKey/repos", classOf[Seq[Repository]]),
            pageRequest = pageRequest
          )()
      case None => client.executePaginated(Request(s"$BASE/$projectKey/repos", classOf[Seq[Repository]]))()
    }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified project.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission to call this resource.
    */
  def findAllUsersWithPermissions(
      projectKey: String,
      pageRequest: Option[PageRequest]
  ): RequestResponse[Seq[UserPermission]] = pageRequest match {
    case Some(pageRequest) =>
      client
        .executePaginatedWithPageRequest(
          Request(s"$BASE/$projectKey/permissions/users", classOf[Seq[UserPermission]]),
          pageRequest = pageRequest
        )()
    case None =>
      client.executePaginated(Request(s"$BASE/$projectKey/permissions/users", classOf[Seq[UserPermission]]))()
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified project, including their avatarUrls.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission to call this resource.
    */
  def findAllUsersWithPermissionsAndAvatars(
      projectKey: String,
      avatarSize: Option[Int],
      pageRequest: Option[PageRequest]
  ): RequestResponse[Seq[UserPermission]] = {
    val parameters = Map("avatarSize" -> avatarSize.getOrElse(64).toString)

    pageRequest match {
      case Some(pageRequest) =>
        client.executePaginatedWithPageRequest(
          Request(s"$BASE/$projectKey/permissions/users", classOf[Seq[UserPermission]]),
          pageRequest
        )(params = parameters)
      case None =>
        client.executePaginated(Request(s"$BASE/$projectKey/permissions/users", classOf[Seq[UserPermission]]))(
          params = parameters
        )
    }
  }

  /**
    * Retrieve a page of groups that have been granted at least one permission for the specified project.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission to call this resource.
    */
  def findAllGroups(projectKey: String, pageRequest: Option[PageRequest]): RequestResponse[Seq[Group]] =
    pageRequest match {
      case Some(pageRequest) =>
        client.executePaginatedWithPageRequest(
          Request(s"$BASE/$projectKey/permissions/groups", classOf[Seq[Group]]),
          pageRequest = pageRequest
        )()
      case None => client.executePaginated(Request(s"$BASE/$projectKey/permissions/groups", classOf[Seq[Group]]))()
    }
}
