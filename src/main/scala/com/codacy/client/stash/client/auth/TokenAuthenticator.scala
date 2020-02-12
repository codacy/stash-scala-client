package com.codacy.client.stash.client.auth

import scalaj.http.HttpRequest

class TokenAuthenticator(token: String) extends Authenticator {
  override def withAuthentication(request: HttpRequest): HttpRequest =
    request.header("Authorization", s"Bearer $token")
}
