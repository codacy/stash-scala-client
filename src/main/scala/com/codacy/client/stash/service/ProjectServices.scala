package com.codacy.client.stash.service

import com.codacy.client.stash.client.{PageRequest, Request, RequestResponse, StashClient}
import com.codacy.client.stash._

class ProjectServices(client: StashClient) {

  val BASE: String = "/rest/api/1.0/projects"

  /**
    * Only projects for which the authenticated user has the PROJECT_VIEW permission will be returned.
    */
  def findById(projectKey: String): RequestResponse[Project] = {
    client.execute(Request(s"$BASE/$projectKey", classOf[Project]))
  }

  /**
    * Only projects for which the authenticated user has the PROJECT_VIEW permission will be returned.
    */
  def findAll(pageRequest: Option[PageRequest]): RequestResponse[Seq[Project]] = pageRequest match {
    case Some(pageRequest) =>
      client.executePaginatedWithPageRequest(Request(BASE, classOf[Seq[Project]]), pageRequest = pageRequest)()
    case None => client.executePaginated(Request(BASE, classOf[Seq[Project]]))()
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
  ): RequestResponse[Seq[UserPermission]] = pageRequest match {
    case Some(pageRequest) =>
      client.executePaginatedWithPageRequest(
        Request(s"$BASE/$projectKey/permissions/users?filter=$user", classOf[Seq[UserPermission]]),
        pageRequest = pageRequest
      )()
    case None =>
      client.executePaginated(
        Request(s"$BASE/$projectKey/permissions/users?filter=$user", classOf[Seq[UserPermission]])
      )()
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
  ): RequestResponse[Seq[UserPermission]] = pageRequest match {
    case Some(pageRequest) =>
      client.executePaginatedWithPageRequest(
        Request(
          s"$BASE/$projectKey/permissions/users?avatarSize=${avatarSize.getOrElse(0)}",
          classOf[Seq[UserPermission]]
        ),
        pageRequest
      )()
    case None =>
      client.executePaginated(
        Request(
          s"$BASE/$projectKey/permissions/users?avatarSize=${avatarSize.getOrElse(0)}",
          classOf[Seq[UserPermission]]
        )
      )()
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
