package com.codacy.client.stash.service

import com.codacy.client.stash.helpers.AuthHelpers
import com.codacy.client.stash.{BuildStatus, CommitStatus}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

class BuildStatusServicesTest extends WordSpec with Matchers with MockitoSugar with AuthHelpers {

  "createBuildStatus" should {
    "create a status with no errors returned" in withClient(basicAuth) { client =>
      (for {
        commit <- sys.env.get("BB_SERVER_COMMIT")
      } yield {
        val service = new BuildStatusServices(client)

        val response = service.createBuildStatus(
          commit,
          new BuildStatus(
            CommitStatus.Successful,
            "stash-status-test",
            "Status Automated Test",
            "http://localhost",
            "Automated testing status"
          )
        )

        response.hasError shouldBe false
      }).getOrElse(fail("Failed to commit to post status"))

    }
  }

}
