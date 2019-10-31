package com.codacy.client.stash.client.auth

import scalaj.http.HttpRequest

trait Authenticator {
  def withAuthentication(request: HttpRequest): HttpRequest
}
