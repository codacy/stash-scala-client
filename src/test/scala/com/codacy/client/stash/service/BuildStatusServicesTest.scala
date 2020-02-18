package com.codacy.client.stash.service

import com.codacy.client.stash.TimestampedBuildStatus._
import com.codacy.client.stash.client.{Request, RequestResponse, StashClient}
import com.codacy.client.stash.{CommitStatus, TimestampedBuildStatus}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.{Matchers, _}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsResult, Json}

class BuildStatusServicesTest extends WordSpec with Matchers with MockitoSugar {

  "getBuildStatus" should {
    "be able to parse a TimestampedBuildStatus" in {
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
      value.fold(e => fail(s"$e"), r => {
        r.state shouldBe CommitStatus.InProgress
        r.key shouldBe "SOMETHING"
        r.name shouldBe "SOMETHING-12"
        r.url shouldBe "https://whatever.com/browse/SOMETHING-12"
        r.description shouldBe "Some description"
        r.dateAdded shouldBe new DateTime(1524576682976L)
      })
    }

    "correctly call the client" in {
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
      val expectedRequestMatch = ArgumentMatchers.eq(expectedRequest)
      val mockResponse = RequestResponse(Some(Seq(clientResponseItem)))
      when(
        client
          .executePaginated[TimestampedBuildStatus](request = expectedRequestMatch)(
            params = ArgumentMatchers.eq(Map.empty[String, String])
          )(ArgumentMatchers.any())
      ).thenReturn(mockResponse)

      // WHEN
      val services = new BuildStatusServices(client)
      val result = services.getBuildStatus(commitHash)

      // THEN
      result.value shouldEqual Some(Seq(clientResponseItem))
      result.hasError shouldBe false
    }
  }

}
