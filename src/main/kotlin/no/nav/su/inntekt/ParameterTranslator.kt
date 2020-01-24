package no.nav.su.inntekt

import io.ktor.http.Parameters
import java.time.YearMonth

internal class ParameterTranslator(private val parameters: Parameters) {
   internal val parsedParameters = when {
      parametereMangler() -> FeilMedParametere("inntektssøk krever fnr, fom, og tom")
      parametereHarFeilFormat() -> FeilMedParametere("fom og tom må være på format 'yyyy-MM'")
      else -> Søkeparametere(parameters.fnr(), parameters.fom(), parameters.tom())
   }

   private fun parametereMangler() =
      !parameters.contains("fnr") || !parameters.contains("fom") || !parameters.contains("tom")

   private fun parametereHarFeilFormat() =
      !parameters["fom"]!!.harYearMonthFormat() || !parameters["tom"]!!.harYearMonthFormat() || parameters["fnr"]!!.length != 11

   private fun Parameters.fom() = this["fom"]!!.tilYearMonth()
   private fun Parameters.tom() = this["tom"]!!.tilYearMonth()
   private fun Parameters.fnr() = this["fnr"]!!
}

internal fun String.harYearMonthFormat() = this.matches("""[0-9]{1,4}-(0[1-9]|1[1-2])""".toRegex())
private fun String.tilYearMonth() = YearMonth.parse(this)

internal sealed class Parametere
internal class Søkeparametere(val fnr: String, val fom: YearMonth, val tom: YearMonth) : Parametere()
internal class FeilMedParametere(val melding: String) : Parametere()
