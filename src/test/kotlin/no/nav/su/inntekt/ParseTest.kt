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

   @Test
   fun `skal produsere omtrent den jsonen vi forventer`() {
      val json = Inntekter(source).toJson()
      assertEquals(expectedResult.onelined(), json.onelined())
   }
}

private fun String.onelined() = this.replace("\t", "").replace("\n", "").replace(" ", "")

val expectedResult = """
{
   "maanedligInntekter": [{
			"gjeldendeMaaned": "2018-01",
			"inntekter": [{
				"beskrivelse": ".",
				"beloep": "1000.5",
				"type": "LOENNSINNTEKT",
				"inntektsperiodetype": ".",
				"utbetaltIMaaned": ".",
				"virksomhet": {
					"identifikator": ".",
					"aktoerType": "."
				}
			}]
		}, {
			"gjeldendeMaaned": "2018-02",
			"inntekter": [{
				"beskrivelse": ".",
				"beloep": "1000.5",
				"type": "LOENNSINNTEKT",
				"inntektsperiodetype": ".",
				"utbetaltIMaaned": ".",
				"virksomhet": {
					"identifikator": ".",
					"aktoerType": "."
				}
			}]
		}, {
			"gjeldendeMaaned": "2018-12",
			"inntekter": [{
				"beskrivelse": ".",
				"beloep": "1000.5",
				"type": "LOENNSINNTEKT",
				"inntektsperiodetype": ".",
				"utbetaltIMaaned": ".",
				"virksomhet": {
					"identifikator": ".",
					"aktoerType": "."
				}
			}]
		},
		{
			"gjeldendeMaaned": "2019-01",
			"inntekter": [{
				"beskrivelse": ".",
				"beloep": "1000.5",
				"type": "LOENNSINNTEKT",
				"inntektsperiodetype": ".",
				"utbetaltIMaaned": ".",
				"virksomhet": {
					"identifikator": ".",
					"aktoerType": "."
				}
			}]
		}
	]
}
""".trimIndent()
