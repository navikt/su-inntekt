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
import org.json.JSONObject
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
         body = mapOf(
            "ident" to mapOf(
               "identifikator" to fnr,
               "aktoerType" to "NATURLIG_IDENT"
            ),
            "ainntektsfilter" to "SupplerendeStoenadA-inntekt",
            "formaal" to "Supplerende",
            "maanedFom" to fom,
            "maanedTom" to tom
         )
      })
   }
}

class Inntekter(source: String) {
   private val maanedligInntekter = JSONObject(source).getJSONArray("arbeidsInntektMaaned").map {
      MaanedligInntekt(it as JSONObject)
   }

   fun totalInntekt(year: Int) = maanedligInntekter.filter { it.year == year }.sumByDouble { it.totalInntekt() }

   fun toJson(): String = """
      {
       "maanedligInntekter": [${maanedligInntekter.map { it.toJson() }.joinToString(",")}]
      }
   """.trimIndent()
}

private class MaanedligInntekt(source: JSONObject) {
   internal val year = source.getString("aarMaaned").subSequence(0, 4).toString().toInt()
   internal val month = source.getString("aarMaaned").subSequence(6, 7).toString().toInt()
   internal val inntekter =
      source.getJSONObject("arbeidsInntektInformasjon").getJSONArray("inntektListe").map { Inntekt(it as JSONObject) }

   internal fun totalInntekt() = inntekter.sumByDouble { it.beloep }
   fun toJson(): String = """
      {
         "year": "$year",
         "month": "$month",
         "inntekter": [${inntekter.map { it.toJson() }.joinToString(",")}]
      }
   """.trimIndent()
}

private class Inntekt(source: JSONObject) {
   internal val beloep = source.getDouble("beloep")
   internal val type = source.getString("inntektType")
   fun toJson(): String = """
      {
         "beloep": "$beloep",
         "type": "$type"
      }
   """.trimIndent()
}
