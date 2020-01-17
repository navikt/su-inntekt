package no.nav.su.inntekt

import org.json.JSONObject
import java.time.YearMonth

class Inntekter(source: String) {
   private val maanedligInntekter = JSONObject(source).getJSONArray("arbeidsInntektMaaned").map {
       MaanedligInntekt(it as JSONObject)
   }

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
         "inntekter": [${inntekter.joinToString(",") { it.toJson() }}],
         "maanedsinntekt": ${totalInntekt()}
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
