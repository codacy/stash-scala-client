package com.codacy.client.stash.client.auth

import java.security._
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

import oauth.signpost.OAuth
import oauth.signpost.exception.OAuthMessageSignerException
import oauth.signpost.http.{HttpParameters, HttpRequest}
import oauth.signpost.signature.{OAuthMessageSigner, SignatureBaseString}

import scala.util.control.NonFatal

class RsaSha1MessageSigner extends OAuthMessageSigner {

  def getSignatureMethod: String = "RSA-SHA1"

  def sign(request: HttpRequest, requestParams: HttpParameters) = {
    try {
      val sbs = new SignatureBaseString(request, requestParams).generate()
      val keyBytes = sbs.getBytes(OAuth.ENCODING)
      val signedBytes = signBytes(keyBytes)
      base64Encode(signedBytes).trim()
    } catch {
      case NonFatal(e) =>
        throw new OAuthMessageSignerException(new Exception(e))
    }
  }

  private def signBytes(keyBytes: Array[Byte]): Array[Byte] = {
    val signer = Signature.getInstance("SHA1withRSA")
    signer.initSign(getPrivateKey)
    signer.update(keyBytes)
    signer.sign()
  }

  private def getPrivateKey: PrivateKey = {
    val privateKeyString = getConsumerSecret
    val privateKeyBytes = Base64.getDecoder.decode(privateKeyString)
    val privateKeyFactory = KeyFactory.getInstance("RSA")
    val privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes)

    privateKeyFactory.generatePrivate(privateKeySpec)
  }

}
