package com.codacy.client.stash.service

import com.codacy.client.stash._
import com.codacy.client.stash.client.StashClient
import com.codacy.client.stash.client.auth.BasicAuthenticator
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, _}
import play.api.libs.json.{JsResult, Json}
import com.codacy.client.stash.helpers.SSHKeyGenerator

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
    val value: JsResult[UserSshKey] = json.validate[UserSshKey]

    // THEN
    value.fold(e => fail(s"$e"), sshKey => {
      sshKey.id shouldBe 1
      sshKey.text shouldBe "ssh-rsa test123"
      sshKey.label shouldBe "test123"
    })
  }

  "UserServices.createUserKey()" should "create a status with no errors returned" in {

    (for {
      baseUrl <- sys.env.get("STASH_URL")
      username <- sys.env.get("STASH_USERNAME")
      password <- sys.env.get("STASH_PASSWORD")
      auth <- Some(new BasicAuthenticator(username, password))
    } yield {
      val service =
        new UserServices(
          new StashClient(
            baseUrl,
            Some(auth)))

      val keys = SSHKeyGenerator.generateKey()
      val response = keys match {
        case (publicKey, _) =>
          service.createUserKey(publicKey)
      }
      response.hasError shouldBe false

      service.deleteUserKey()

    }).getOrElse(fail("Missing auth properties"))
  }

}
