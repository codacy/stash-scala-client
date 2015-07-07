package com.codacy.client.stash.client.auth

import _root_.oauth.signpost.AbstractOAuthConsumer
import play.api.libs.oauth._
import play.api.libs.ws._

/**
 * A signature calculator for the Play WS API.
 *
 * Example:
 * {{{
 * WS.url("http://example.com/protected").sign(OAuthCalculator(service, tokens)).get()
 * }}}
 */
case class OAuthRSACalculator(consumerKey: ConsumerKey, token: RequestToken) extends AbstractOAuthConsumer(consumerKey.key, consumerKey.secret) with WSSignatureCalculator {

  import _root_.oauth.signpost.http.HttpRequest

  this.setTokenWithSecret(token.token, token.secret)
  this.setMessageSigner(new RsaSha1MessageSigner())

  override protected def wrap(request: Any) = request match {
    case r: WSRequest => new WSRequestAdapter(r)
    case _ => throw new IllegalArgumentException("OAuthCalculator expects requests of type play.api.libs.ws.WSRequest")
  }

  override def sign(request: WSRequest): Unit = sign(wrap(request))

  class WSRequestAdapter(request: WSRequest) extends HttpRequest {

    import scala.collection.JavaConverters._

    override def unwrap() = request

    override def getAllHeaders: java.util.Map[String, String] =
      request.allHeaders.map { entry => (entry._1, entry._2.headOption) }
        .filter { entry => entry._2.isDefined }
        .map { entry => (entry._1, entry._2.get) }.asJava

    override def getHeader(name: String): String = request.header(name).getOrElse("")

    override def getContentType: String = getHeader("Content-Type")

    override def getMessagePayload = new java.io.ByteArrayInputStream(request.getBody.getOrElse(Array.emptyByteArray))

    override def getMethod: String = this.request.method

    override def setHeader(name: String, value: String) {
      request.setHeader(name, value)
    }

    /**
     * Returns the full URL with query string for correct signing.
     * @return a URL with query string attached.
     */
    override def getRequestUrl = request.url

    override def setRequestUrl(url: String) {
      request.setUrl(url)
    }

  }

}
