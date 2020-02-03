package com.codacy.client.stash.service

import com.codacy.client.stash.Project
import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import play.api.libs.json.Json

class ProjectServices(client: StashClient) {

  /**
   * Only projects for which the authenticated user has the PROJECT_VIEW permission will be returned.
   */
  def getProjects: RequestResponse[Seq[Project]] = {
    (client executePaginated Request("/rest/api/1.0/projects", classOf[Seq[Project]]))(Json.reads[Project])
  }
}
