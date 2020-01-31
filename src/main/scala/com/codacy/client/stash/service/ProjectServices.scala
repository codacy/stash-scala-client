package com.codacy.client.stash.service

import com.codacy.client.stash.TimestampedBuildStatus
import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}

class ProjectServices(client: StashClient) {

  /**
   * Only projects for which the authenticated user has the PROJECT_VIEW permission will be returned.
   */
  def getProjects: RequestResponse[Seq[String]] = {
    client.executePaginated(Request(urlPath(), classOf[Seq[String]]))
  }

  private def urlPath(): String = s"/rest/api/1.0/projects"

}
