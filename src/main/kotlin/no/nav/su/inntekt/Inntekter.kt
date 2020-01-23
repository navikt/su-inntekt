package no.nav.su.inntekt

import org.json.JSONObject
import java.time.YearMonth

sealed class InntektResultat

class Feil(val kode: Int, val beskrivelse: String): InntektResultat()

class Inntekter(source: String): InntektResultat() {
   private val maanedligInntekter = JSONObject(source).optJSONArray("arbeidsInntektMaaned")?.map {
       MaanedligInntekt(it as JSONObject)
   } ?: emptyList()

   fun totalInntekt(year: Int) = maanedligInntekter.filter { it.validForYear(year) }.sumByDouble { it.totalInntekt() }
   fun toJson(): String = """
      {
       "maanedligInntekter": [${maanedligInntekter.joinToString(",") { it.toJson() }}]
      }
   """.trimIndent()
}

private class MaanedligInntekt(source: JSONObject) {
   private val yearMonth = source.getString("aarMaaned")
   private val inntekter =
      source.getJSONObject("arbeidsInntektInformasjon").getJSONArray("inntektListe").map {
          Inntekt(
              it as JSONObject
          )
      }

   internal fun validForYear(year: Int) = year == YearMonth.parse(this.yearMonth).year
   internal fun totalInntekt() = inntekter.sumByDouble { it.beloep }
   fun toJson(): String = """
      {
         "gjeldendeMaaned": "$yearMonth",
         "inntekter": [${inntekter.joinToString(",") { it.toJson() }}]
      }
   """.trimIndent()
}

private class Inntekt(private val source: JSONObject) {
   internal val beloep = source.getDouble("beloep")

   fun toJson(): String = """
      {
         "beskrivelse": "${source["beskrivelse"]}",
         "beloep": "$beloep",
         "type": "${source["inntektType"]}",
         "inntektsperiodetype": "${source["inntektsperiodetype"]}",
         "utbetaltIMaaned": "${source["utbetaltIMaaned"]}",
         "virksomhet": {
           "identifikator": "${source.getJSONObject("virksomhet")["identifikator"]}",
           "aktoerType": "${source.getJSONObject("virksomhet")["aktoerType"]}"
         }
      }
   """.trimIndent()
}
