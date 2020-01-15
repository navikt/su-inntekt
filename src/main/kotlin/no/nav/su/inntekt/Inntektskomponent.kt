package no.nav.su.inntekt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import no.nav.su.inntekt.sts.STS
import java.time.YearMonth

class Inntektskomponent(
   private val baseUrl: String,
   private val httpClient: HttpClient = simpleHttpClient(),
   private val stsRestClient: STS
) {
   suspend fun hentInntektsliste(
      aktørId: String,
      fom: YearMonth,
      tom: YearMonth,
      callId: String
   ) =
      httpClient.request<HttpResponse>("$baseUrl/api/v1/hentinntektliste") {
         method = HttpMethod.Post
         header("Authorization", "Bearer ${stsRestClient.token()}")
         header("Nav-Consumer-Id", "supstonad")
         header("Nav-Call-Id", callId)
         contentType(ContentType.Application.Json)
         accept(ContentType.Application.Json)
         body = mapOf(
            "ident" to mapOf(
               "identifikator" to aktørId,
               "aktoerType" to "AKTOER_ID"
            ),
            "ainntektsfilter" to "SupplerendeStoenadA-inntekt",
            "formaal" to "Supplerende",
            "maanedFom" to fom,
            "maanedTom" to tom
         )
      }
         .let { toMånedListe(objectMapper.readValue(it.readText())) }
}

private fun toMånedListe(node: JsonNode) = node["arbeidsInntektMaaned"].map(::tilMåned)

private fun toInntekt(node: JsonNode) = Inntekt(
   beløp = node["beloep"].asDouble(),
   inntektstype = Inntektstype.valueOf(node["inntektType"].textValue()),
   orgnummer = node["virksomhet"].let {
      if (it["aktoerType"].asText() == "ORGANISASJON") {
         it["identifikator"].asText()
      } else {
         null
      }
   }
)

private fun tilMåned(node: JsonNode) = Måned(
   YearMonth.parse(node["aarMaaned"].asText()),
   node["arbeidsInntektInformasjon"]["inntektListe"].map(::toInntekt)
)

data class Måned(
   val årMåned: YearMonth,
   val inntektsliste: List<Inntekt>
)

data class Inntekt(
   val beløp: Double,
   val inntektstype: Inntektstype,
   val orgnummer: String?
)

enum class Inntektstype {
   LOENNSINNTEKT,
   NAERINGSINNTEKT,
   PENSJON_ELLER_TRYGD,
   YTELSE_FRA_OFFENTLIGE
}

val objectMapper: ObjectMapper = jacksonObjectMapper()
   .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
   .registerModule(JavaTimeModule())


private fun simpleHttpClient() = HttpClient() {
   install(JsonFeature) {
      this.serializer = JacksonSerializer {
         registerModule(JavaTimeModule())
         disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      }
   }
}
