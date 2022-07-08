package com.codacy.client.stash.client

import java.net.URL

import codacy.foundation.utils.Logger
import com.codacy.client.stash.client.auth.Authenticator
import com.codacy.client.stash.util.HTTPStatusCodes
import play.api.libs.json._
import scalaj.http.{Http, HttpOptions, HttpRequest, HttpResponse, StringBodyConnectFunc}

import scala.util.Properties
import scala.util.control.NonFatal

class StashClient(apiUrl: String, authenticator: Option[Authenticator] = None, acceptAllCertificates: Boolean = false) extends Logger {

  /*
   * Does an API request and parses the json output into a class
   */
  def execute[T](
      request: Request[T]
  )(params: Map[String, String] = Map.empty)(implicit reader: Reads[T]): RequestResponse[T] = {
    get[T](request.path, params) match {
      case Right(json) => RequestResponse(json.asOpt[T])
      case Left(error) => error
    }
  }

  /*
   * Does an paginated API request and parses the json output into a sequence of classes
   */
  def executePaginated[T](
      request: Request[Seq[T]]
  )(params: Map[String, String] = Map.empty)(implicit reader: Reads[T]): RequestResponse[Seq[T]] = {
    val cleanUrl = request.path.takeWhile(_ != '?')
    get[Seq[T]](cleanUrl, params) match {
      case Right(json) =>
        val nextRepos = (for {
          isLastPage <- (json \ "isLastPage").asOpt[Boolean] if !isLastPage
          nextPageStart <- (json \ "nextPageStart").asOpt[Int]
        } yield {
          executePaginated(Request(cleanUrl, request.classType))(params + ("start" -> nextPageStart.toString)).value
            .getOrElse(Seq.empty)
        }).getOrElse(Seq.empty)

        RequestResponse(Some((json \ "values").as[Seq[T]] ++ nextRepos))

      case Left(resp) => resp
    }
  }

  def executePaginatedWithPageRequest[T](request: Request[Seq[T]], pageRequest: PageRequest)(
      params: Map[String, String] = Map.empty
  )(implicit reader: Reads[T]): RequestResponse[Seq[T]] = {
    val cleanUrl = request.path.takeWhile(_ != '?')

    get[Seq[T]](
      cleanUrl,
      params ++ Map("start" -> pageRequest.getStart.toString, "limit" -> pageRequest.getLimit.toString)
    ) match {
      case Right(json) =>
        RequestResponse(
          value = Some((json \ "values").as[Seq[T]]),
          nextPageStart = (json \ "nextPageStart").asOpt[Int],
          size = (json \ "size").asOpt[Int],
          limit = (json \ "limit").asOpt[Int],
          isLastPage = (json \ "isLastPage").asOpt[Boolean]
        )
      case Left(error) =>
        error
    }
  }

  def postJson[T](request: Request[T], values: JsValue)(implicit reader: Reads[T]): RequestResponse[T] = {
    performRequest("POST", request, values)
  }

  def putJson[T](request: Request[T], values: JsValue)(implicit reader: Reads[T]): RequestResponse[T] = {
    performRequest("PUT", request, values)
  }

  //   Wrap request with authentication options
  private def withAuthentication(request: HttpRequest): HttpRequest =
    authenticator match {
      case Some(auth) => auth.withAuthentication(request)
      case None => request
    }

  /*
   * Does an API request
   */
  private def performRequest[T](method: String, request: Request[T], values: JsValue)(
      implicit reader: Reads[T]
  ): RequestResponse[T] = {
    doRequest[T](request.path, method, Map.empty, Option(values)) match {
      case Right((HTTPStatusCodes.OK | HTTPStatusCodes.CREATED, body)) =>
        parseJson[T](body).fold(identity, { jsValue =>
          valueOrError[T](jsValue)
        })

      case Right((HTTPStatusCodes.NO_CONTENT, _)) =>
        RequestResponse[T](None)

      case Right((statusCode, body)) =>
        getError[T](statusCode, statusCode.toString, body)

      case Left(requestResponse) =>
        requestResponse
    }
  }

  def delete(requestUrl: String)(params: Map[String, String] = Map.empty): RequestResponse[Boolean] = {
    doRequest[Boolean](requestUrl, "DELETE", params, None) match {
      case Right((HTTPStatusCodes.NO_CONTENT, _)) =>
        RequestResponse[Boolean](Option(true))

      case Right((HTTPStatusCodes.OK, body)) =>
        parseJson[JsObject](body).fold({ error =>
          RequestResponse[Boolean](None, message = error.message, hasError = true)
        }, { _ =>
          RequestResponse[Boolean](Option(true))
        })

      case Right((statusCode, body)) =>
        getError[Boolean](statusCode, statusCode.toString, body)

      case Left(requestResponse) =>
        requestResponse
    }
  }

  private def get[T](
      requestUrl: String,
      params: Map[String, String] = Map.empty
  ): Either[RequestResponse[T], JsValue] = {
    doRequest[T](requestUrl, "GET", params, None) match {
      case Right((HTTPStatusCodes.OK | HTTPStatusCodes.CREATED, body)) =>
        parseJson[T](body)
      case Right((statusCode, body)) =>
        Left(getError[T](statusCode, statusCode.toString, body))
      case Left(error) =>
        Left(error)
    }
  }

  def doRequest[T](
      requestPath: String,
      method: String,
      params: Map[String, String] = Map.empty,
      payload: Option[JsValue] = None
  ): Either[RequestResponse[T], (Int, String)] = {
    val url = generateUrl(requestPath)
    logger.info("[StashClient::doRequest] url: "+url)
    try {
      val baseRequestWithoutCertificatesOption = Http(url).method(method).params(params)

      val baseRequest =
        if (acceptAllCertificates) {
          baseRequestWithoutCertificatesOption.option(HttpOptions.allowUnsafeSSL)
        } else {
          baseRequestWithoutCertificatesOption
        }

      val request = payload
        .fold(baseRequest)(
          p =>
            // Supports PUT and POST of JSON
            baseRequest
              .header("content-type", "application/json")
              .copy(connectFunc = StringBodyConnectFunc(Json.stringify(p)))
        )
      val authenticatedRequest = withAuthentication(request)
      val response = authenticatedRequest.asString

      //if the response code is a redirect, follow it
      if (HTTPStatusCodes.Redirects.all.contains(response.code)) {
        followRedirect(method, params, payload, response)
      } else {
        Right((response.code, response.body))
      }
    } catch {
      case NonFatal(exception) =>
        Left(RequestResponse[T](value = None, message = exception.getMessage, hasError = true))
    }
  }

  /**
    * Checks the new location on the header of the [[HttpResponse]] passed by parameter
    * and follows the redirect by making a new request. It returns the response if it is unable
    * to get the location from the header.
    * @param method The method of the request
    * @param params The parameters of the request
    * @param payload The payload of the request
    * @param response The response to extract the new location or to return as default.
    */
  private def followRedirect[T](
      method: String,
      params: Map[String, String],
      payload: Option[JsValue],
      response: HttpResponse[String]
  ): Either[RequestResponse[T], (Int, String)] = {
    response.headers.get("Location") match {
      case Some(Vector(newLocation)) =>
        logger.info("[StashClient::followRedirect] newLocation: "+newLocation)
        val newPath = new URL(newLocation).getPath
        doRequest(newPath, method, params, payload)
      case _ => Right((response.code, response.body))
    }
  }

  private def valueOrError[T](json: JsValue)(implicit reader: Reads[T]): RequestResponse[T] = {
    json.validate[T] match {
      case s: JsSuccess[T] =>
        RequestResponse(Some(s.value))

      case e: JsError =>
        val msg =
          s"""|Failed to validate json:
              |$json
              |JsError errors:
              |${e.errors.mkString(System.lineSeparator)}
                """.stripMargin
        RequestResponse[T](None, message = msg, hasError = true)
    }
  }

  private def getError[T](status: Int, statusText: String, body: String): RequestResponse[T] = {
    val msg =
      s"""|$status: $statusText
          |Body:
          |$body
           """.stripMargin
    RequestResponse[T](None, message = msg, hasError = true)
  }

  private def parseJson[T](input: String): Either[RequestResponse[T], JsValue] = {
    val json = Json.parse(input)

    val errorOpt = (json \ "errors")
      .asOpt[Seq[ResponseError]]
      .map(_.map { error =>
        s"""|Context: ${error.context.getOrElse("None")}
            |Exception: ${error.exceptionName.getOrElse("None")}
            |Message: ${error.message}
         """.stripMargin
      }.mkString(Properties.lineSeparator))

    errorOpt
      .map { error =>
        Left(RequestResponse[T](None, message = error, hasError = true))
      }
      .getOrElse(Right(json))
  }

  private def generateUrl(endpoint: String) = {
    s"$apiUrl$endpoint"
  }

}
