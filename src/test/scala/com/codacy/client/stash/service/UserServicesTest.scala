package com.codacy.client.stash.service

import com.codacy.client.stash._
import org.scalatest.{Matchers, _}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsResult, Json}

class UserServicesTest extends WordSpec with Matchers with MockitoSugar {

  "createUserKey" should {
    "be able to parse a SshKey" in {
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
  }

}
