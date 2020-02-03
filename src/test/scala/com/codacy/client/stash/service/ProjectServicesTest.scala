package com.codacy.client.stash.service

import com.codacy.client.stash.client.auth.TokenAuthenticator
import com.codacy.client.stash.client.{RequestResponse, StashClient}
import com.codacy.client.stash.{Permission, Project}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

class ProjectServicesTest extends WordSpec with Matchers with MockitoSugar {

  val accessToken = ""

  "getProjects" should {
    "correctly get projects" in {
      val authenticator = new TokenAuthenticator(accessToken)
      val client = new StashClient("http://localhost:7990", Option(authenticator))

      val services = new ProjectServices(client)
      val result: RequestResponse[Seq[Project]] = services.getProjects

      if (result.hasError) {
        println("Error ", result.message)
      } else {
        result.value.map(response => println("projects", response))
      }
    }

    "correctly get permissions" in {
      val authenticator = new TokenAuthenticator(accessToken)
      val client = new StashClient("http://localhost:7990", Option(authenticator))

      val services = new ProjectServices(client)
      val result: RequestResponse[Seq[Permission]] = services.getProjectPermissions("TP2")

      if (result.hasError) {
        println("Error ", result.message)
      } else {
        result.value.map(response => response.foreach(permission => {
          println(permission)
        }))
      }
    }
  }
}