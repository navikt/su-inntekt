package no.nav.su.inntekt

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.github.tomakehurst.wiremock.WireMockServer
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpHeaders.XRequestId
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import java.util.*

const val AZURE_REQUIRED_GROUP = "su-gruppa"
const val AZURE_CLIENT_ID = "clientId"
const val AZURE_ISSUER = "azure"
const val AZURE_WELL_KNOWN_URL = "/wellknown"
const val DEFAULT_CALL_ID = "callId"
const val STS_USERNAME = "srvsupstonad"
const val STS_PASSWORD = "supersecret"

@KtorExperimentalAPI
fun Application.testEnv(wireMockServer: WireMockServer? = null) {
   val baseUrl = wireMockServer?.let { it.baseUrl() } ?: ""
   (environment.config as MapApplicationConfig).apply {
      put("azure.requiredGroup", AZURE_REQUIRED_GROUP)
      put("azure.clientId", AZURE_CLIENT_ID)
      put("azure.wellKnownUrl", "$baseUrl$AZURE_WELL_KNOWN_URL")
      put("sts.baseUrl", baseUrl)
      put("sts.username", STS_USERNAME)
      put("sts.password", STS_PASSWORD)
      put("inntektUrl", baseUrl)
   }
}

val jwtStub = JwtStub()
@KtorExperimentalAPI
internal fun Application.usingMocks(
   jwkConfig: JSONObject = mockk(relaxed = true),
   jwkProvider: JwkProvider = mockk(relaxed = true),
   inntektskomponent: InntektskomponentClient = mockk(relaxed = true)
) {
   val e = Base64.getEncoder().encodeToString(jwtStub.publicKey.publicExponent.toByteArray())
   val n = Base64.getEncoder().encodeToString(jwtStub.publicKey.modulus.toByteArray())
   every {
      jwkProvider.get(any())
   }.returns(Jwk("key-1234", "RSA", "RS256", null, emptyList(), null, null, null, mapOf("e" to e, "n" to n)))
   every {
      jwkConfig.getString("issuer")
   }.returns(AZURE_ISSUER)

   every {
      inntektskomponent.hentInntektsliste(any(), any())
   } returns (Inntekter(inntektJson))

   suinntekt(
      jwkConfig = jwkConfig,
      jwkProvider = jwkProvider,
      inntekt = inntektskomponent
   )
}

fun TestApplicationEngine.withCallId(
   method: HttpMethod,
   uri: String,
   setup: TestApplicationRequest.() -> Unit = {}
): TestApplicationCall {
   return handleRequest(method, uri) {
      addHeader(XRequestId, DEFAULT_CALL_ID)
      setup()
   }
}

private val inntektJson = """
   {
     "arbeidsInntektMaaned": [
       {
         "aarMaaned": "2020-01",
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
               "utbetaltIMaaned": "2020-01",
               "opplysningspliktig": {
                 "identifikator": "873152362",
                 "aktoerType": "ORGANISASJON"
               },
               "virksomhet": {
                 "identifikator": "873152362",
                 "aktoerType": "ORGANISASJON"
               },
               "inntektsmottaker": {
                 "identifikator": "27088921696",
                 "aktoerType": "NATURLIG_IDENT"
               },
               "inngaarIGrunnlagForTrekk": true,
               "utloeserArbeidsgiveravgift": true,
               "informasjonsstatus": "InngaarAlltid",
               "beskrivelse": "fastloenn"
             },
             {
               "inntektType": "LOENNSINNTEKT",
               "beloep": 2000,
               "fordel": "kontantytelse",
               "inntektskilde": "A-ordningen",
               "inntektsperiodetype": "Maaned",
               "inntektsstatus": "LoependeInnrapportert",
               "leveringstidspunkt": "2020-01",
               "utbetaltIMaaned": "2020-01",
               "opplysningspliktig": {
                 "identifikator": "873152362",
                 "aktoerType": "ORGANISASJON"
               },
               "virksomhet": {
                 "identifikator": "873152362",
                 "aktoerType": "ORGANISASJON"
               },
               "inntektsmottaker": {
                 "identifikator": "27088921696",
                 "aktoerType": "NATURLIG_IDENT"
               },
               "inngaarIGrunnlagForTrekk": true,
               "utloeserArbeidsgiveravgift": true,
               "informasjonsstatus": "InngaarAlltid",
               "beskrivelse": "kapitalInntekt"
             },
             {
               "inntektType": "YTELSE_FRA_OFFENTLIGE",
               "beloep": 5000,
               "fordel": "kontantytelse",
               "inntektskilde": "A-ordningen",
               "inntektsperiodetype": "Maaned",
               "inntektsstatus": "LoependeInnrapportert",
               "leveringstidspunkt": "2020-01",
               "utbetaltIMaaned": "2020-01",
               "opplysningspliktig": {
                 "identifikator": "873152362",
                 "aktoerType": "ORGANISASJON"
               },
               "virksomhet": {
                 "identifikator": "873152362",
                 "aktoerType": "ORGANISASJON"
               },
               "inntektsmottaker": {
                 "identifikator": "27088921696",
                 "aktoerType": "NATURLIG_IDENT"
               },
               "inngaarIGrunnlagForTrekk": true,
               "utloeserArbeidsgiveravgift": true,
               "informasjonsstatus": "InngaarAlltid",
               "beskrivelse": "ufoeretrygd"
             }
           ]
         }
       }
     ],
     "ident": {
       "identifikator": "27088921696",
       "aktoerType": "NATURLIG_IDENT"
     }
   }
""".trimIndent()
