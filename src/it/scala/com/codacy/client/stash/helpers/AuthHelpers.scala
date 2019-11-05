package com.codacy.client.stash.helpers

import com.codacy.client.stash.client.StashClient
import com.codacy.client.stash.client.auth.{Authenticator, BasicAuthenticator, OAuth1Authenticator}
import org.scalatest.WordSpec

trait AuthHelpers {
  self: WordSpec =>

  val basicAuth: Authenticator = {
    (for {
      username <- sys.env.get("BB_SERVER_USERNAME")
      password <- sys.env.get("BB_SERVER_PASSWORD")
    } yield {
      new BasicAuthenticator(username, password)
    }).getOrElse(fail("Failed to obtain configurations to instantiate the BasicAuthenticator"))
  }

  val oauth1: Authenticator = {
    (for {
      authKey <- sys.env.get("BB_SERVER_CONSUMER_KEY")
      authSecret <- sys.env.get("BB_SERVER_CONSUMER_SECRET")
      token <- sys.env.get("BB_SERVER_AUTH_TOKEN")
      tokenSecret <- sys.env.get("BB_SERVER_AUTH_SECRET")
    } yield {
      new OAuth1Authenticator(authKey, authSecret, token, tokenSecret)
    }).getOrElse(fail("Failed to obtain configurations to instantiate the OAuth1Authenticator"))
  }

  def withClient[T](authenticator: Authenticator)(body: StashClient => T): T = {
    (for {
      baseUrl <- sys.env.get("BB_SERVER_URL")
    } yield {
      val client = new StashClient(baseUrl, Option(authenticator))
      body(client)
    }).getOrElse(fail("Failed to obtain configurations to instantiate the client"))
  }

}
