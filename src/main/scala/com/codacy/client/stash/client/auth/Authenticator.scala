package com.codacy.client.stash.client.auth

import play.api.libs.ws.WSRequest

trait Authenticator {
  def withAuthentication(request: WSRequest): WSRequest
}
