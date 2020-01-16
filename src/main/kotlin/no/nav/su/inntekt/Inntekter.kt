package no.nav.su.inntekt

import org.json.JSONObject

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
      source.getJSONObject("arbeidsInntektInformasjon").getJSONArray("inntektListe").map {
          Inntekt(
              it as JSONObject
          )
      }

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
