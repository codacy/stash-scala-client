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
    get(request.url) match {
      case Right(json) => RequestResponse(json.asOpt[T])
      case Left(error) => RequestResponse(None, error.message, hasError = true)
    }
  }

  /*
   * Does an paginated API request and parses the json output into a sequence of classes
   */
  def executePaginated[T](request: Request[Seq[T]])(implicit reader: Reads[T]): RequestResponse[Seq[T]] = {
    get(request.url) match {
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

      case Left(error) =>
        RequestResponse[Seq[T]](None, error.message, hasError = true)
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

      val value = if (Seq(HTTPStatusCodes.OK, HTTPStatusCodes.CREATED).contains(result.status)) {
        val body = result.body

        val jsValue = parseJson(body)
        jsValue match {
          case Right(json) =>
            json.validate[T] match {
              case s: JsSuccess[T] =>
                RequestResponse(Some(s.value))

              case e: JsError =>
                val msg =
                  s"""
                     |Failed to validate json:
                     |$json
                     |JsError errors:
                     |${e.errors.mkString(System.lineSeparator)}
                """.stripMargin
                RequestResponse[T](None, message = msg, hasError = true)
            }

          case Left(message) =>
            RequestResponse[T](None, message = message.message, hasError = true)
        }
      } else {
        RequestResponse[T](None, message = result.statusText, hasError = true)
      }

      value
    }
  }

  /* copy paste from post ... */
  def delete[T](requestUrl: String): RequestResponse[Boolean] = withClient {
    client =>
      val url = generateUrl(requestUrl)
      val jpromise = withAuthentication(
        client.url(url).withFollowRedirects(follow = true)
      ).delete()
      val result = Await.result(jpromise, requestTimeout)

      val value = if (Seq(HTTPStatusCodes.OK, HTTPStatusCodes.CREATED, HTTPStatusCodes.NO_CONTENT).contains(result.status)) {
        RequestResponse(Option(true))
      } else {
        RequestResponse[Boolean](None, result.statusText, hasError = true)
      }
      value
  }

  private def get(requestUrl: String): Either[ResponseError, JsValue] = withClient {
    client =>
      val url = generateUrl(requestUrl)
      val jpromise = withAuthentication(
        client.url(url).withFollowRedirects(follow = true)
      ).get()
      val result = Await.result(jpromise, requestTimeout)

      val value = if (Seq(HTTPStatusCodes.OK, HTTPStatusCodes.CREATED).contains(result.status)) {
        val body = result.body
        parseJson(body)
      } else {
        Left(ResponseError(java.util.UUID.randomUUID().toString, result.statusText, result.statusText))
      }
      value
  }

  private def parseJson(input: String): Either[ResponseError, JsValue] = {
    val json = Json.parse(input)

    val errorOpt = (json \ "errors").asOpt[Seq[ResponseError]].flatMap(_.headOption)

    errorOpt.map {
      error =>
        Left(error)
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
