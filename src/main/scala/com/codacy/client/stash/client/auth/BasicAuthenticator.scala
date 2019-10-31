package com.codacy.client.stash.client.auth
import scalaj.http.HttpRequest

class BasicAuthenticator(username: String, password: String) extends Authenticator {
  override def withAuthentication(request: HttpRequest): HttpRequest =
    request.auth(username, password)
}
