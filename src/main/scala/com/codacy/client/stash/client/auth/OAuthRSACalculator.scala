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

  override protected def wrap(request: Any): HttpRequest = request match {
    case r: WSRequest => new WSRequestAdapter(r)
    case _ => throw new IllegalArgumentException("OAuthCalculator expects requests of type play.api.libs.ws.WSRequest")
  }

  def sign(request: WSRequest): Unit = sign(wrap(request))

  class WSRequestAdapter(request: WSRequest) extends HttpRequest {

    import scala.collection.JavaConversions._

    override def unwrap() = request

    override def getAllHeaders = {
      request.headers.flatMap{ case (key,values) => values.headOption.map((key,_)) }
    }

    override def getHeader(name: String): String = {
      request.headers.collectFirst{ case (name,values) => values.headOption }.flatten.getOrElse("")
    }

    override def getContentType: String = getHeader("Content-Type")

    override def getMessagePayload = {
      val bytes = request.body match{
        case InMemoryBody(bytes) => bytes
        case _ => Array.emptyByteArray
      }
      new java.io.ByteArrayInputStream(bytes)
    }

    override def getMethod: String = this.request.method

    override def setHeader(name: String, value: String) = {
      request.withHeaders((name,value))
    }

    /**
     * Returns the full URL with query string for correct signing.
     * @return a URL with query string attached.
     */

    //this needs to be a var to implement java getters and setters of the interface
    private[this] var url_ = request.url

    override def getRequestUrl = url_

    override def setRequestUrl(url: String) = {
      url_ = url
    }

  }

}
