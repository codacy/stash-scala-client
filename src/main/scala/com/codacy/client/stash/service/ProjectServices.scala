package com.codacy.client.stash.service

import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash.{Group, Permission, Project, User}

class ProjectServices(client: StashClient) {

  val BASE: String = "/rest/api/1.0/projects"

  /**
    * Only projects for which the authenticated user has the PROJECT_VIEW permission will be returned.
    */
  def findAll: RequestResponse[Seq[Project]] = {
    client executePaginated Request(BASE, classOf[Seq[Project]])
  }

  /**
    * Retrieve a page of users that have been granted at least one permission for the specified project.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission
    * to call this resource.
    */
  def findAllUserPermissions(projectKey: String): RequestResponse[Seq[Permission]] = {
    client executePaginated Request(s"$BASE/$projectKey/permissions/users", classOf[Seq[Permission]])
  }

  /**
    * Retrieve a page of groups that have been granted at least one permission for the specified project.
    *
    * The authenticated user must have PROJECT_ADMIN permission for the specified project or a higher global permission
    * to call this resource.
    */
  def findAllGroupPermission(projectKey: String): RequestResponse[Seq[Group]] = {
    client executePaginated Request(s"$BASE/$projectKey/permissions/groups", classOf[Seq[Group]])
  }

  /**
    * Retrieve a page of users that have been granted at least one global permission.
    *
    * The authenticated user must have ADMIN permission or higher to call this resource.
    */
  def findUserProjectPermission(projectKey: String, name: String): RequestResponse[Seq[Permission]] = {
    client executePaginated Request(s"$BASE/$projectKey/permissions/users?filter=$name", classOf[Seq[Permission]])
  }
}
