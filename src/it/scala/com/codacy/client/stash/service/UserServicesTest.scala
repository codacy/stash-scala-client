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

  "getUser" should {
    "return a user when an username doesn't contains special characters" in withClient(basicAuth) { client =>
      val username = "dev"
      val service = new UserServices(client)

      val response = service.getUser(username)

      response.hasError shouldBe false
      response.value.map(_.username) shouldBe Some(username)
    }

    "fallback to getUsers and return a user when an username contains special characters" in withClient(basicAuth) {
      client =>
        val username = "integrationtest3@codacy.com"
        val service = new UserServices(client)

        val response = service.getUser(username)

        response.hasError shouldBe false
        response.value.map(_.name) shouldBe Some(username)
    }
  }
}
