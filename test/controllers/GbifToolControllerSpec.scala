package controllers

import models.Species
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import services.{GbifParser, GbifService}

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class GbifToolControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "GbifToolController GET" should {

    "return a list of species in JSON format" in {
      val request = FakeRequest(GET, "/list")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must include ("""{"latinName":"Accipiter gentilis"}""")
    }

    "match exact name" in {
      val request = FakeRequest(PUT, "/match/Acanthagenys_rufogularis")
      val resp = route(app, request).get

      status(resp) mustBe ACCEPTED
      contentType(resp) mustBe Some("application/json")
      contentAsString(resp) must include("\"canonicalName\":\"Acanthagenys rufogularis\"")
    }

    "match Ovis aries" in {
      val request = FakeRequest(PUT, "/match/Ovis_aries")
      val resp = route(app, request).get

      status(resp) mustBe ACCEPTED
      contentType(resp) mustBe Some("application/json")
      contentAsString(resp) must include("\"canonicalName\":\"Ovis aries\"")
    }
  }
}
