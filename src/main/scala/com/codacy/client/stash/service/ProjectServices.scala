package com.codacy.client.stash.service

import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash._

class ProjectServices(client: StashClient) {

  val BASE: String = "/rest/api/1.0/projects"

  /**
    * Only projects for which the authenticated user has the PROJECT_VIEW permission will be returned.
    */
  def findAll: RequestResponse[Seq[Project]] = {
    client.executePaginated(Request(BASE, classOf[Seq[Project]]))
  }

  /**
    * Only projects for which the authenticated user has the PROJECT_VIEW permission will be returned.
    */
  def findById(projectKey: String): RequestResponse[Project] = {
    client.execute(Request(s"$BASE/$projectKey", classOf[Project]))
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified project.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission to call this resource.
    */
  def findUserPermissions(projectKey: String, user: String): RequestResponse[Seq[UserPermission]] = {
    client.executePaginated(Request(s"$BASE/$projectKey/permissions/users?filter=$user", classOf[Seq[UserPermission]]))
  }

  /**
    * Retrieve repositories from the project corresponding to the supplied projectKey.
    *
    * The authenticated user must have REPO_READ permission for the specified project to call this resource.
    */
  def findAllRepositories(projectKey: String): RequestResponse[Seq[Repository]] = {
    client.executePaginated(Request(s"$BASE/$projectKey/repos", classOf[Seq[Repository]]))
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified project.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission to call this resource.
    */
  def findAllUsersWithPermissions(projectKey: String): RequestResponse[Seq[UserPermission]] = {
    client.executePaginated(Request(s"$BASE/$projectKey/permissions/users", classOf[Seq[UserPermission]]))
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified project, including their avatarUrls.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission to call this resource.
    */
  def findAllUsersWithPermissionsAndAvatars(projectKey: String, size: Option[Int]): RequestResponse[Seq[UserPermission]] = {
    client.executePaginated(Request(
      s"$BASE/$projectKey/permissions/users?avatarSize=${size.getOrElse(0)}",
      classOf[Seq[UserPermission]]
    ))
  }

  /**
    * Retrieve a page of groups that have been granted at least one permission for the specified project.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission to call this resource.
    */
  def findAllGroups(projectKey: String): RequestResponse[Seq[Group]] = {
    client.executePaginated(Request(s"$BASE/$projectKey/permissions/groups", classOf[Seq[Group]]))
  }

  /**
    * Retrieve the avatar for the project matching the supplied projectKey.
    *
    * The authenticated user must have PROJECT_VIEW permission for the specified project to call this resource.
    */
  def findAvatar(projectKey: String, size: Option[Int]): RequestResponse[String] = {
    client.execute(Request(s"$BASE/$projectKey/avatar?${size.getOrElse(0)}", classOf[String]))
  }

}
