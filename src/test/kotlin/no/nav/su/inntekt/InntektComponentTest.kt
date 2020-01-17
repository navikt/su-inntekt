package no.nav.su.inntekt

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.ContentType.Application.FormUrlEncoded
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@KtorExperimentalAPI
internal class InntektComponentTest {

   companion object {
      private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
      private val jwtStub by lazy {
         JwtStub(wireMockServer)
      }
      private val stsStub by lazy {
         StsStub(wireMockServer)
      }

      @BeforeAll
      @JvmStatic
      fun start() {
         wireMockServer.start()
         WireMock.stubFor(jwtStub.stubbedJwkProvider())
         WireMock.stubFor(jwtStub.stubbedConfigProvider())
         WireMock.stubFor(stsStub.stubbedSTS())
      }

      @AfterAll
      @JvmStatic
      fun stop() {
         wireMockServer.stop()
      }
   }

   @Test
   fun `hent inntekt`() {
      WireMock.stubFor(
         WireMock.post(urlPathEqualTo("/api/v1/hentinntektliste"))
            .withHeader(Authorization, equalTo("Bearer $STS_TOKEN"))
            .withHeader("Nav-Consumer-Id", equalTo("supstonad"))
            .withHeader("Nav-Call-Id", AnythingPattern())
            .willReturn(
               WireMock.okJson(inntekterResponse)
            )
      )

      val token = jwtStub.createTokenFor()
      withTestApplication({
         testEnv(wireMockServer)
         suinntekt()
      }) {
         withCallId(Post, INNTEKT_PATH) {
            addHeader(Authorization, "Bearer $token")
            addHeader(ContentType, FormUrlEncoded.toString())
            setBody("fnr=01010112345&fom=2018-01&tom=2018-12")
         }
      }.apply {
         assertEquals(OK, response.status())
         println(response.content)
      }
   }

   private val inntekterResponse = """
    {
        "arbeidsInntektMaaned": [
            {
                "aarMaaned": "2018-12",
                "arbeidsInntektInformasjon": {
                    "inntektListe": [
                        {
                            "inntektType": "LOENNSINNTEKT",
                            "beloep": 25000,
                            "fordel": "kontantytelse",
                            "inntektskilde": "A-ordningen",
                            "inntektsperiodetype": "Maaned",
                            "inntektsstatus": "LoependeInnrapportert",
                            "leveringstidspunkt": "2020-01",
                            "utbetaltIMaaned": "2018-12",
                            "opplysningspliktig": {
                                "identifikator": "orgnummer1",
                                "aktoerType": "ORGANISASJON"
                            },
                            "virksomhet": {
                                "identifikator": "orgnummer1",
                                "aktoerType": "ORGANISASJON"
                            },
                            "inntektsmottaker": {
                                "identifikator": "aktørId",
                                "aktoerType": "AKTOER_ID"
                            },
                            "inngaarIGrunnlagForTrekk": true,
                            "utloeserArbeidsgiveravgift": true,
                            "informasjonsstatus": "InngaarAlltid",
                            "beskrivelse": "fastloenn"
                        }
                    ]
                }
            },
            {
                "aarMaaned": "2019-05",
                "arbeidsInntektInformasjon": {
                    "inntektListe": [
                        {
                            "inntektType": "LOENNSINNTEKT",
                            "beloep": 25000,
                            "fordel": "kontantytelse",
                            "inntektskilde": "A-ordningen",
                            "inntektsperiodetype": "Maaned",
                            "inntektsstatus": "LoependeInnrapportert",
                            "leveringstidspunkt": "2020-01",
                            "utbetaltIMaaned": "2019-05",
                            "opplysningspliktig": {
                                "identifikator": "orgnummer2",
                                "aktoerType": "ORGANISASJON"
                            },
                            "virksomhet": {
                                "identifikator": "orgnummer2",
                                "aktoerType": "ORGANISASJON"
                            },
                            "inntektsmottaker": {
                                "identifikator": "aktørId",
                                "aktoerType": "AKTOER_ID"
                            },
                            "inngaarIGrunnlagForTrekk": true,
                            "utloeserArbeidsgiveravgift": true,
                            "informasjonsstatus": "InngaarAlltid",
                            "beskrivelse": "fastloenn"
                        }
                    ]
                }
            }
        ],
        "ident": {
            "identifikator": "aktørId",
            "aktoerType": "AKTOER_ID"
        }
    }
"""

}


