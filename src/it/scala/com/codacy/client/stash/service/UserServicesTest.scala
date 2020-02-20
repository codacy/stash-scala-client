package com.codacy.client.stash.service

import com.codacy.client.stash.helpers.{AuthHelpers, SSHKeyGenerator}
import org.scalatest.{Ignore, Matchers, WordSpec}
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

    /**
      * This test depends on the correct environment variables being set:
      * "BB_SERVER_CONSUMER_KEY", "BB_SERVER_CONSUMER_SECRET", "BB_SERVER_AUTH_TOKEN", "BB_SERVER_AUTH_SECRET"
      *
      * This test will be ignored for now since it's constantly failing because of changes in our
      * bitbucket server instance. Therefore will be ignored and will be reenable on the task CY-862.
      */
    /*"(using oauth1 tokens) create a status with no errors returned" in withClient(oauth1) { client =>
      val service = new UserServices(client)

      val (publicKey, _) = SSHKeyGenerator.generateKey()
      val response = service.createUserKey(publicKey)

      response.hasError shouldBe false
      response.value.map { key =>
        service.deleteUserKey(key.id)
      }
    }*/
  }
}
