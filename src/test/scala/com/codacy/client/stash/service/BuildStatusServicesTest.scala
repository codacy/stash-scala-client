package com.codacy.client.stash.service;

import com.codacy.client.stash.{BuildStatus, CommitStatus, TimestampedBuildStatus}
import com.codacy.client.stash.TimestampedBuildStatus._
import com.codacy.client.stash.client.auth.BasicAuthenticator
import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import org.joda.time.DateTime
import org.scalatest._
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsResult, Json}

class BuildStatusServicesTest extends FlatSpec with Matchers with MockitoSugar {

  "BuildStatusServices.getBuildStatus()" should "be able to parse a TimestampedBuildStatus" in {
    // HAVING
    val jsonText =
      """
        |{
        |  "state": "INPROGRESS",
        |  "key": "SOMETHING",
        |  "name": "SOMETHING-12",
        |  "url": "https://whatever.com/browse/SOMETHING-12",
        |  "description": "Some description",
        |  "dateAdded": 1524576682976
        |}
      """.stripMargin

    // WHEN
    val json = Json.parse(jsonText)
    val value: JsResult[TimestampedBuildStatus] = json.validate[TimestampedBuildStatus]

    // THEN
    value.fold(e =>
      fail(s"$e"),
      r => {
        r.state shouldBe CommitStatus.InProgress
        r.key shouldBe "SOMETHING"
        r.name shouldBe "SOMETHING-12"
        r.url  shouldBe "https://whatever.com/browse/SOMETHING-12"
        r.description shouldBe "Some description"
        r.dateAdded shouldBe new DateTime(1524576682976L)
      }
    )
  }

  it should "correctly call the client" in {
    // HAVING
    val commitHash = "some-hash"
    val expectedUrl = s"/rest/build-status/1.0/commits/$commitHash"
    val clientResponseItem = TimestampedBuildStatus(
      state = CommitStatus.InProgress,
      key = "SOMETHING",
      name = "SOMETHING-12",
      url = "https://whatever.com/browse/SOMETHING-12",
      description = "Some description",
      dateAdded = new DateTime(1524576682976L)
    )

    val client = mock[StashClient]
    val expectedRequest = Request[Seq[TimestampedBuildStatus]](expectedUrl, classOf[Seq[TimestampedBuildStatus]])
    val expectedRequestMatch = org.mockito.Matchers.eq(expectedRequest)
    val mockResponse= RequestResponse(Some(Seq(clientResponseItem)))
    when(client.executePaginated[TimestampedBuildStatus](expectedRequestMatch)(anyObject())).thenReturn(mockResponse)

    // WHEN
    val services = new BuildStatusServices(client)
    val result = services.getBuildStatus(commitHash)

    // THEN
    result.value shouldEqual Some(Seq(clientResponseItem))
    result.hasError shouldBe false
  }

  "BuildStatusServices.createBuildStatus()" should "create a status with no errors returned" in {

    (for {
      baseUrl <- sys.env.get("STASH_URL")
      username <- sys.env.get("STASH_USERNAME")
      password <- sys.env.get("STASH_PASSWORD")
      commit <- sys.env.get("STASH_COMMIT")
      auth <- Some(new BasicAuthenticator(username, password))
    } yield {
      val service = new BuildStatusServices(new StashClient(baseUrl, Some(auth)))
      val response = service.createBuildStatus(commit, new BuildStatus(CommitStatus.Successful,
        "stash-status-test", "Status Automated Test", "http://localhost", "Automated testing status"))

      response.hasError shouldBe false

    }).getOrElse(fail("Missing auth properties"))


  }

}
