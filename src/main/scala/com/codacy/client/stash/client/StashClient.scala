package com.codacy.client.stash.client

import com.codacy.client.stash.client.auth.{Authenticator, OAuth1Authenticator}
import com.codacy.client.stash.util.HTTPStatusCodes
import com.ning.http.client.{AsyncHttpClient, AsyncHttpClientConfigBean}
import play.api.http.{ContentTypeOf, Writeable}
import play.api.libs.json._
import play.api.libs.ws.WSRequest
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Properties, Try}

class StashClient(baseUrl: String, authenticator: Option[Authenticator] = None, acceptAllCertificates: Boolean = false) {
  // Keep these 2 constructors just for backward compatibility
  @deprecated("Please pass an OAuth1Authenticator(key, secretKey, token secretToken) into StashClient.", "3.0.4")
  def this(baseUrl: String, key: String, secretKey: String, token: String, secretToken: String) =
    this(baseUrl, Some(new OAuth1Authenticator(key, secretKey, token, secretToken)))

  @deprecated("Please pass an OAuth1Authenticator(key, secretKey, token secretToken) into StashClient.", "3.0.4")
  def this(baseUrl: String, key: String, secretKey: String, token: String, secretToken: String, acceptAllCertificates: Boolean) =
    this(baseUrl, Some(new OAuth1Authenticator(key, secretKey, token, secretToken)), acceptAllCertificates)

  private lazy val requestTimeout = Duration(10, SECONDS)

  /*
   * Does an API request and parses the json output into a class
   */
  def execute[T](request: Request[T])(implicit reader: Reads[T]): RequestResponse[T] = {
    get[T](request.url) match {
      case Right(json) => RequestResponse(json.asOpt[T])
      case Left(error) => error
    }
  }

  /*
   * Does an paginated API request and parses the json output into a sequence of classes
   */
  def executePaginated[T](request: Request[Seq[T]])(implicit reader: Reads[T]): RequestResponse[Seq[T]] = {
    get[Seq[T]](request.url) match {
      case Right(json) =>
        val nextRepos = (for {
          isLastPage <- (json \ "isLastPage").asOpt[Boolean] if !isLastPage
          nextPageStart <- (json \ "nextPageStart").asOpt[Int]
        } yield {
          val cleanUrl = request.url.takeWhile(_ != '?')
          val nextUrl = s"$cleanUrl?start=$nextPageStart"
          executePaginated(Request(nextUrl, request.classType)).value.getOrElse(Seq.empty)
        }).getOrElse(Seq.empty)

        RequestResponse(Some((json \ "values").as[Seq[T]] ++ nextRepos))

      case Left(resp) => resp
    }
  }

  def postForm[T](request: Request[T], values: Map[String, Seq[String]])(implicit reader: Reads[T]): RequestResponse[T] = {
    performRequest("POST", request, values)
  }

  def postJson[T](request: Request[T], values: JsValue)(implicit reader: Reads[T]): RequestResponse[T] = {
    performRequest("POST", request, values)
  }

  def putForm[T](request: Request[T], values: Map[String, Seq[String]])(implicit reader: Reads[T]): RequestResponse[T] = {
    performRequest("PUT", request, values)
  }

  def putJson[T](request: Request[T], values: JsValue)(implicit reader: Reads[T]): RequestResponse[T] = {
    performRequest("PUT", request, values)
  }

  // Wrap request with authentication options
  private def withAuthentication(request: WSRequest): WSRequest = authenticator match {
    case Some(auth) => auth.withAuthentication(request)
    case None => request
  }

  /*
   * Does an API request
   */
  private def performRequest[D, T](method: String, request: Request[T], values: D)
                                  (implicit reader: Reads[T], writer: Writeable[D], contentType: ContentTypeOf[D]): RequestResponse[T] = {
    val url = generateUrl(request.url)

    withClient { client =>
      val jpromise = withAuthentication(
        client
          .url(url)
          .withFollowRedirects(follow = true)
          .withMethod(method)
          .withBody(values)
      ).execute()
      val result = Await.result(jpromise, requestTimeout)

      Try(result.body).map {
        case body if Seq(HTTPStatusCodes.OK, HTTPStatusCodes.CREATED).contains(result.status) =>
          parseJson[T](body) match {
            case Right(json) => valueOrError[T](json)
            case Left(response) => response
          }

        case body =>
          getError[T](result.status, result.statusText, body)
      }
        .getOrElse(getError[T](result.status, result.statusText))
    }
  }

  def delete(requestUrl: String): RequestResponse[Boolean] = withClient {
    client =>
      val url = generateUrl(requestUrl)
      val jpromise = withAuthentication(
        client.url(url).withFollowRedirects(follow = true)
      ).delete()
      val result = Await.result(jpromise, requestTimeout)

      if (HTTPStatusCodes.NO_CONTENT == result.status) {
        RequestResponse[Boolean](Option(true))
      } else {
        Try(result.body)
          .map {
            case body if Seq(HTTPStatusCodes.OK).contains(result.status) =>
              parseJson[JsObject](body) match {
                case Right(_) => RequestResponse[Boolean](Option(true))
                case Left(resp) => RequestResponse[Boolean](None, resp.message, hasError = true)
              }

            case body =>
              getError[Boolean](result.status, result.statusText, body)
          }
          .getOrElse(getError[Boolean](result.status, result.statusText))
      }
  }

  private def get[T](requestUrl: String): Either[RequestResponse[T], JsValue] = withClient {
    client =>
      val url = generateUrl(requestUrl)
      val jpromise = withAuthentication(
        client.url(url).withFollowRedirects(follow = true)
      ).get()
      val result = Await.result(jpromise, requestTimeout)

      Try(result.body)
        .map {
          case body if Seq(HTTPStatusCodes.OK, HTTPStatusCodes.CREATED).contains(result.status) =>
            parseJson[T](body)
          case body =>
            Left(getError[T](result.status, result.statusText, body))
        }
        .getOrElse(Left(getError[T](result.status, result.statusText)))
  }

  private def valueOrError[T](json: JsValue)(implicit reader: Reads[T]) = {
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

  private def getError[T](status: Int, statusText: String, body: String = "Failed to read response body") = {
    val msg =
      s"""|$status: $statusText
          |Body:
          |$body
           """.stripMargin
    RequestResponse[T](None, msg, hasError = true)
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

    errorOpt.map { error =>
      Left(RequestResponse[T](None, error, hasError = true))
    }.getOrElse(Right(json))
  }

  private def generateUrl(endpoint: String) = s"$baseUrl$endpoint"

  private def withClient[T](block: NingWSClient => T): T = {

    val config = if (acceptAllCertificates) {
      new AsyncHttpClientConfigBean().setAcceptAnyCertificate(true).setFollowRedirect(true)
    } else {
      new AsyncHttpClient().getConfig
    }

    val client = NingWSClient(config)
    val result = block(client)
    client.close()
    result
  }

}
