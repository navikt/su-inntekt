package no.nav.su.inntekt

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class YearMonthTest {
   @Test
   fun `noen forskjellige strenger som kan være YearMonth`() {
      assertTrue("2010-01".harYearMonthFormat())
      assertTrue("0001-01".harYearMonthFormat())
      assertTrue("1-01".harYearMonthFormat())
      assertTrue("9999-12".harYearMonthFormat())
   }

   @Test
   fun `noen forskjellige strenger som ikke kan være YearMonth`() {
      assertFalse("2020-13".harYearMonthFormat())
      assertFalse("12-2001".harYearMonthFormat())
   }

}
