package com.codacy.client.stash.service

import com.codacy.client.stash.client.auth.TokenAuthenticator
import com.codacy.client.stash.client.{RequestResponse, StashClient}
import com.codacy.client.stash.{Group, Permission, Project}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

/**
  * Small integration test for development purposes.
  */
class ProjectServicesTest extends WordSpec with Matchers with MockitoSugar {

  val testEndpoint = "http://localhost:7990"

  val testAccessToken = ""

  val testProjectKey = "TP"

  "getProjects" should {
    "correctly get projects" in {
      val services = new ProjectServices(getClient)
      val result: RequestResponse[Seq[Project]] = services.findAll

      printResult("projects", result)
    }

    "correctly get explicit user permissions" in {
      val services = new ProjectServices(getClient)
      val result: RequestResponse[Seq[Permission]] = services.findAllUserPermissions(testProjectKey)

      printResult("user permissions", result)
    }

    "correctly get explicit group permissions" in {
      val services = new ProjectServices(getClient)
      val result: RequestResponse[Seq[Group]] = services.findAllGroupPermission(testProjectKey)

      printResult("group permissions", result)
    }

    "correctly get user project permissions" in {
      val client = getClient
      val userServices = new UserServices(client)
      val projectServices = new ProjectServices(client)

      val name: String = userServices.getUsername.value.get
      val result: RequestResponse[Seq[Permission]] = projectServices.findUserProjectPermission(testProjectKey, name)

      printResult("user permissions", result)
    }
  }

  private def getClient: StashClient = {
    val authenticator = new TokenAuthenticator(testAccessToken)
    val client = new StashClient(testEndpoint, Option(authenticator))

    val result: RequestResponse[String] = new UserServices(client).getUsername

    printResult("user", result)

    client
  }

  private def printResult[T](test: String, result: RequestResponse[T]): Unit = {
    println(s"---- ${test.toUpperCase} ----")

    if (result.hasError) {
      println("Error: ", result.message)
    } else {
      result.value.foreach(response => println(response))
    }
  }
}
