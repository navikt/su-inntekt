package no.nav.su.inntekt

import com.github.kittinunf.fuel.httpPost
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import no.nav.su.inntekt.sts.STS
import org.slf4j.MDC
import java.time.YearMonth

class InntektskomponentClient(
   private val baseUrl: String,
   private val stsRestClient: STS
) {
   fun hentInntektsliste(
      fnr: String,
      fom: YearMonth,
      tom: YearMonth,
      callId: String
   ): Inntekter {
      val (_, _, result) = "$baseUrl/api/v1/hentinntektliste".httpPost()
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
            "identifikator": "$fnr",
            "aktoerType":"NATURLIG_IDENT"
            },
            "ainntektsfilter": "SupplerendeStoenadA-inntekt",
            "formaal": "Supplerende",
            "maanedFom": "$fom",
            "maanedTom": "$tom"
            }
         """.trimIndent()
         ).responseString()
      return Inntekter(result.get())
   }
}
