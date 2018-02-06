package com.codacy.client.stash.client.auth
import play.api.libs.ws.{WSAuthScheme, WSRequest}

class BasicAuthenticator(username: String, password: String) extends Authenticator {
  override def withAuthentication(request: WSRequest) = request.withAuth(username, password, WSAuthScheme.BASIC)
}
