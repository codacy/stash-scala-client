package com.codacy.client.stash.client.auth
import scalaj.http.{HttpRequest, Token}

class OAuth1Authenticator(key: String, secretKey: String, token: String, secretToken: String) extends Authenticator {

  override def withAuthentication(request: HttpRequest): HttpRequest = {
    OAuth1.sign(request, Token(key, secretKey), Option(Token(token, secretToken)), None)
  }

}
