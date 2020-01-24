package no.nav.su.inntekt

import com.github.kittinunf.fuel.httpPost
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import kotlinx.coroutines.runBlocking
import no.nav.su.inntekt.sts.STS
import org.slf4j.MDC
import java.time.YearMonth

internal class InntektskomponentClient(
   private val baseUrl: String,
   private val stsRestClient: STS
) {
   internal fun hentInntektsliste(
      params: Søkeparametere,
      callId: String
   ): InntektResultat {
      val (_, response, result) = "$baseUrl/api/v1/hentinntektliste".httpPost()
         .header(HttpHeaders.Authorization, "Bearer ${stsRestClient.token()}")
         .header(HttpHeaders.XRequestId, MDC.get(HttpHeaders.XRequestId))
         .header(HttpHeaders.ContentType, ContentType.Application.Json)
         .header(HttpHeaders.Accept, ContentType.Application.Json)
         .header("Nav-Call-Id", callId)
         .header("Nav-Consumer-Id", "supstonad")
         .body(
            """
            {
            "ident": {
            "identifikator": "${params.fnr}",
            "aktoerType":"NATURLIG_IDENT"
            },
            "ainntektsfilter": "SupplerendeStoenadA-inntekt",
            "formaal": "Supplerende",
            "maanedFom": "${params.fom}",
            "maanedTom": "${params.tom}"
            }
         """.trimIndent()
         ).responseString()

      return if (response.statusCode >= 400) {
         Feil(response.statusCode, response.responseMessage)
      } else {
         Inntekter(result.get())
      }
   }
}
