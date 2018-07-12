package com.codacy.client.stash.service

import com.codacy.client.stash.TimestampedBuildStatus._
import com.codacy.client.stash.client.auth.BasicAuthenticator
import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash._
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.{Matchers, _}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsResult, Json}

class UserServicesTest extends FlatSpec with Matchers with MockitoSugar {

  "UserServices.createUserKey" should "be able to parse a SshKey" in {
    // HAVING
    val jsonText =
      """
        |{
        | "id": 1,
        | "text": "ssh-rsa test123",
        | "label": "test123"
        |}
      """.stripMargin

    // WHEN
    val json = Json.parse(jsonText)
    val value: JsResult[SshKeyByUSer] = json.validate[SshKeyByUSer]

    // THEN
    value.fold(e =>
      fail(s"$e"),
      sshKey => {
        sshKey.id shouldBe 1
        sshKey.text shouldBe "ssh-rsa test123"
        sshKey.label shouldBe "test123"
      }
    )
  }

  "UserServices.createUserKey()" should "create a status with no errors returned" in {

    (for {
      baseUrl <- sys.env.get("STASH_URL")
      username <- sys.env.get("STASH_USERNAME")
      password <- sys.env.get("STASH_PASSWORD")
      auth <- Some(new BasicAuthenticator(username, password))
    } yield {
      val service =
        new UserServices(new StashClient(baseUrl, Some(auth)))

      val response =
        service.createUserKey("1234")

      response.hasError shouldBe false

    }).getOrElse(fail("Missing auth properties"))

  }

}
