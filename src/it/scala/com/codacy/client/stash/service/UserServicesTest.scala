package com.codacy.client.stash.service

import com.codacy.client.stash.helpers.{AuthHelpers, SSHKeyGenerator}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

class UserServicesTest extends WordSpec with Matchers with MockitoSugar with AuthHelpers {

  "createUserKey" should {
    "(using basic auth) create a status with no errors returned" in withClient(basicAuth) { client =>
      val service = new UserServices(client)

      val keys = SSHKeyGenerator.generateKey()

      val response = keys match {
        case (publicKey, _) =>
          service.createUserKey(publicKey)
      }

      response.hasError shouldBe false
      response.value.map { key =>
        service.deleteUserKey(key.id)
      }
    }

    "(using oauth1 tokens) create a status with no errors returned" in withClient(oauth1) { client =>
      val service = new UserServices(client)

      val (publicKey, _) = SSHKeyGenerator.generateKey()
      val response = service.createUserKey(publicKey)

      response.hasError shouldBe false
      response.value.map { key =>
        service.deleteUserKey(key.id)
      }
    }
  }

}
