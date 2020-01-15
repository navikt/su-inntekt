package no.nav.su.inntekt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ParseTest {
   val source = File(ParseTest::class.java.getResource("/inntekt.json").path).readText()

   @Test
   fun `skal parse en superenkel inntekt-response og kalkulere rett inntekt`() {
      val inntekt = Inntekter(source)
      assertEquals(3001.50, inntekt.totalInntekt(2018))
   }
}
