package com.codacy.client.stash.client.auth

/** scalaj.http
  Copyright 2010 Jonathan Hoffman
  Modified by Rodrigo Fernandes (@rtfpessoa) to support OAuth1 with SHA1withRSA

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
import java.net.{URI, URL}
import java.security._
import java.security.spec.PKCS8EncodedKeySpec
import java.util.UUID

import scalaj.http._

/** Utility methods used by [[scalaj.http.HttpRequest]] */
object OAuth1 {

  def sign(req: HttpRequest, consumer: Token, token: Option[Token], verifier: Option[String]): HttpRequest = {
    req.option(conn => {
      val baseParams: Seq[(String, String)] = Seq(
        ("oauth_timestamp", (System.currentTimeMillis / 1000).toString),
        ("oauth_nonce", UUID.randomUUID().toString)
      )

      var (oauthParams, signature) = getSig(baseParams, req, consumer, token, verifier)

      oauthParams +:= (("oauth_signature", signature))
      conn.setRequestProperty("Authorization", s"OAuth ${oauthParams
        .map { case (k, v) => s"""$k="${percentEncode(v)}"""" }
        .mkString(", ")}")
    })
  }

  private def getSig(
      baseParams: Seq[(String, String)],
      req: HttpRequest,
      consumer: Token,
      token: Option[Token],
      verifier: Option[String]
  ): (Seq[(String, String)], String) = {
    var oauthParams = ("oauth_version", "1.0") +: ("oauth_consumer_key", consumer.key) +: (
      "oauth_signature_method",
      "RSA-SHA1"
    ) +: baseParams

    token.foreach(t => oauthParams +:= (("oauth_token", t.key)))

    verifier.foreach(v => oauthParams +:= (("oauth_verifier", v)))

    // OAuth1.0 specifies that only querystring and x-www-form-urlencoded body parameters should be included in signature
    // req.params from multi-part requests are included in the multi-part request body and should NOT be included
    val allTheParams = if (req.connectFunc.isInstanceOf[MultiPartConnectFunc]) {
      oauthParams
    } else {
      req.params ++ oauthParams
    }

    val baseString = Seq(req.method.toUpperCase, normalizeUrl(new URL(req.url)), normalizeParams(allTheParams))
      .map(percentEncode)
      .mkString("&")

    val privatekey = loadPrivateKey(consumer)

    val signature = signBytes(baseString, privatekey)

    (oauthParams, signature)
  }

  private def normalizeParams(params: Seq[(String, String)]): String = {
    percentEncode(params).sortWith(_ < _).mkString("&")
  }

  private def normalizeUrl(url: URL): String = {
    val uri = new URI(url.toString)
    val scheme = uri.getScheme.toLowerCase()
    var authority = uri.getAuthority.toLowerCase()
    val dropPort = (scheme.equals("http") && uri.getPort == 80) || (scheme
      .equals("https") && uri.getPort == 443)
    if (dropPort) {
      // Find the last : in the authority
      val index = authority.lastIndexOf(":")
      if (index >= 0) {
        authority = authority.substring(0, index)
      }
    }
    var path = uri.getRawPath
    if (path == null || path.length() <= 0) {
      path = "/" // Conforms to RFC 2616 section 3.2.2
    }
    // We know that there is no query and no fragment here.
    s"$scheme://$authority$path"
  }

  private def percentEncode(params: Seq[(String, String)]): Seq[String] = {
    params.map { case (key, value) => s"${percentEncode(key)}=${percentEncode(value)}" }
  }

  private def percentEncode(s: String): String = {
    if (s == null) { "" } else {
      HttpConstants
        .urlEncode(s, HttpConstants.utf8)
        .replace("+", "%20")
        .replace("*", "%2A")
        .replace("%7E", "~")
    }
  }

  private def signBytes(text: String, privateKey: PrivateKey): String = {
    val textBytes = text.getBytes(HttpConstants.utf8)
    val signer = Signature.getInstance("SHA1withRSA")
    signer.initSign(privateKey)
    signer.update(textBytes)
    val signedBytes = signer.sign
    HttpConstants.base64(signedBytes).trim
  }

  private def loadPrivateKey(consumer: Token): PrivateKey = {
    val privateKeyBytes = Base64.decode(consumer.secret)
    val privateKeyFactory = KeyFactory.getInstance("RSA")
    val privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes)
    privateKeyFactory.generatePrivate(privateKeySpec)
  }

}
