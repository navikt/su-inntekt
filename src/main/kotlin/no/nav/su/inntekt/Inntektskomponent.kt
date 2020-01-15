package no.nav.su.inntekt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
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
}

class Inntekter(source: String) {
   private val månedligInntekter = objectMapper.readTree(source).get("arbeidsInntektMaaned").toList().map { MaanedligInntekt(it) }
   fun totalInntekt(year: Int) = månedligInntekter.filter { it.year == year }.sumByDouble { it.totalInntekt() }
}
private class MaanedligInntekt(source: JsonNode) {
   internal val year = source.get("aarMaaned").textValue().subSequence(0, 4).toString().toInt()
   internal val month = source.get("aarMaaned").textValue().subSequence(6, 7).toString().toInt()
   internal val inntekter = source.get("arbeidsInntektInformasjon").get("inntektListe").toList().map { Inntekt(it) }
   internal fun totalInntekt() = inntekter.sumByDouble { it.beløp }
}
private class Inntekt(source: JsonNode) {
   internal val beløp = source.get("beloep").asDouble()
   internal val type = source.get("inntektType").textValue()
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
