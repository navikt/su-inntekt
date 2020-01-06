package no.nav.su.inntekt.nais

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import no.nav.su.inntekt.JwtStub
import no.nav.su.inntekt.inntekt
import no.nav.su.inntekt.issuer
import no.nav.su.inntekt.testEnvironment
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

@KtorExperimentalAPI
internal class NaisRoutesComponentTest {

   @Test
   fun naisRoutes() {
      withTestApplication({
         inntekt(testEnvironment(wireMockServer))
      }) {
         handleRequest(Get, IS_ALIVE_PATH)
      }.apply {
         assertEquals(OK, response.status())
         assertEquals("ALIVE", response.content)
      }

      withTestApplication({
         inntekt(testEnvironment(wireMockServer))
      }) {
         handleRequest(Get, IS_READY_PATH)
      }.apply {
         assertEquals(OK, response.status())
         assertEquals("READY", response.content)
      }
   }

   companion object {
      private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
      private val jwtStub by lazy {
         JwtStub(issuer, wireMockServer)
      }

      @BeforeAll
      @JvmStatic
      fun start() {
         wireMockServer.start()
         WireMock.stubFor(jwtStub.stubbedJwkProvider())
         WireMock.stubFor(jwtStub.stubbedConfigProvider())
      }

      @AfterAll
      @JvmStatic
      fun stop() {
         wireMockServer.stop()
      }
   }
}
