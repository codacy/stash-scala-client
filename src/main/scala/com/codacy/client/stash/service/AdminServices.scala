package com.codacy.client.stash.service

import com.codacy.client.stash.User
import com.codacy.client.stash.client.{PageRequest, Request, RequestResponse, StashClient}

class AdminServices(client: StashClient) {

  val BASE: String = "/rest/api/1.0/admin"

  /**
    * Retrieves a list of users that are members of a specified group.
    *
    * The authenticated user must have the LICENSED_USER permission to call this resource.
    */
  def findUsersInGroup(
      context: String,
      filter: Option[String],
      pageRequest: Option[PageRequest]
  ): RequestResponse[Seq[User]] = {
    val baseParameters = Map("context" -> context)

    val parameters = filter.fold(baseParameters) { filter =>
      baseParameters + ("filter" -> filter)
    }

    pageRequest match {
      case Some(pageRequest) =>
        client.executePaginatedWithPageRequest(
          Request(s"$BASE/groups/more-members", classOf[Seq[User]]),
          pageRequest = pageRequest
        )(params = parameters)
      case None =>
        client.executePaginated(Request(s"$BASE/groups/more-members", classOf[Seq[User]]))(params = parameters)
    }
  }
}
