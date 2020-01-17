package no.nav.su.inntekt

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import no.nav.su.inntekt.sts.STS
import java.time.YearMonth

class Inntektskomponent(
   private val baseUrl: String,
   private val httpClient: HttpClient = HttpClient(),
   private val stsRestClient: STS
) {
   fun hentInntektsliste(
      fnr: String,
      fom: YearMonth,
      tom: YearMonth,
      callId: String
   ) = runBlocking {
      Inntekter(httpClient.request("$baseUrl/api/v1/hentinntektliste") {
         method = HttpMethod.Post
         header("Authorization", "Bearer ${stsRestClient.token()}")
         header("Nav-Consumer-Id", "supstonad")
         header("Nav-Call-Id", callId)
         contentType(ContentType.Application.Json)
         accept(ContentType.Application.Json)
         body = """
            {
            "ident": {
               "identifikator": "$fnr",
               "aktoerType": "NATURLIG_IDENT"
               },
            "ainntektsfilter": "SupplerendeStoenadA-inntekt",
            "formaal": "Supplerende",
            "maandedFom": "$fom",
            "maanedTom": "$tom"
            }
         """.trimIndent()
      })
   }
}

