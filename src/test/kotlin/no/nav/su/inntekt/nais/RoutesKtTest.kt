package no.nav.su.inntekt.nais

import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RoutesKtTest {

    @Test
    fun hello() {
        testServer {
            val (_, response, result) = "http://localhost:8088/inntekt".httpGet().responseString()
            assertEquals(HttpStatusCode.OK.value, response.statusCode)
            assertEquals("A million dollars", result.get())
        }
    }

}
