package com.codacy.client.stash.client.auth
import com.ning.http.client.oauth.{ConsumerKey, RequestToken}
import play.api.libs.ws.WSRequest

class OAuth1Authenticator(key: String, secretKey: String, token: String, secretToken: String) extends Authenticator {
  private lazy val KEY = new ConsumerKey(key, secretKey)
  private lazy val TOKEN = new RequestToken(token, secretToken)

  private lazy val requestSigner = new WSSignatureCalculatorRSA(KEY, TOKEN)

  override def withAuthentication(request: WSRequest): WSRequest = request.sign(requestSigner)
}
