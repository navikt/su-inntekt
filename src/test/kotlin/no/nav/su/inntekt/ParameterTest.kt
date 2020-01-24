package no.nav.su.inntekt

import io.ktor.http.ParametersBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class ParameterTest {
   @Test
   fun `korrekte parametere skal parses korrekt`() {
      val builder = ParametersBuilder()
      builder.append("fnr", "01010112345")
      builder.append("fom", "2019-01")
      builder.append("tom", "2020-01")
      val params = builder.build()
      val parsedParameters = ParameterTranslator(params).parsedParameters

      assertTrue { parsedParameters is Søkeparametere }
   }

   @Test
   fun `feil formaterte parametere skal parses til feil`() {
      val builder = ParametersBuilder()
      builder.append("fnr", "01010112345")
      builder.append("fom", "2019--01")
      builder.append("tom", "2020-01")
      val params = builder.build()
      val parsedParameters = ParameterTranslator(params).parsedParameters

      assertTrue { parsedParameters is FeilMedParametere }
   }

   @Test
   fun `feil formaterte fnr skal parses til feil`() {
      val builder = ParametersBuilder()
      builder.append("fnr", "0101112345")
      builder.append("fom", "2019-01")
      builder.append("tom", "2020-01")
      val params = builder.build()
      val parsedParameters = ParameterTranslator(params).parsedParameters

      assertTrue { parsedParameters is FeilMedParametere }
   }

   @Test
   fun `manglende parametere skal parses til feil`() {
      val builder = ParametersBuilder()
      builder.append("fnr", "01010112345")
      builder.append("tom", "2020-01")
      val params = builder.build()
      val parsedParameters = ParameterTranslator(params).parsedParameters

      assertTrue { parsedParameters is FeilMedParametere }
   }
}
