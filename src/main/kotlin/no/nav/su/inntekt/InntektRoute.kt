package no.nav.su.inntekt

import io.ktor.application.call
import io.ktor.features.callId
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.getOrFail
import org.apache.http.client.methods.RequestBuilder.post
import java.time.YearMonth

fun Route.inntektRoute(inntekt: InntektskomponentClient) {
   post("/inntekt") {
      val params = call.receiveParameters()
      val inntekter = inntekt.hentInntektsliste(
         params.getOrFail("fnr"),
         YearMonth.parse(params.getOrFail("fom")),
         YearMonth.parse(params.getOrFail("tom")),
         call.callId!!
      )
      call.respond(HttpStatusCode.OK, inntekter.toJson())
   }
}
