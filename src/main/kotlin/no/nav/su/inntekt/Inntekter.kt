package no.nav.su.inntekt

import org.json.JSONObject

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
   private val year = source.getString("aarMaaned").subSequence(0, 4).toString().toInt()
   private val month = source.getString("aarMaaned").subSequence(6, 7).toString().toInt()
   private val inntekter =
      source.getJSONObject("arbeidsInntektInformasjon").getJSONArray("inntektListe").map {
          Inntekt(
              it as JSONObject
          )
      }

   internal fun validForYear(year: Int) = year == this.year
   internal fun totalInntekt() = inntekter.sumByDouble { it.beloep }
   fun toJson(): String = """
      {
         "year": "$year",
         "month": "$month",
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
