package no.nav.su.inntekt

import io.ktor.application.call
import io.ktor.features.callId
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import java.time.YearMonth

const val INNTEKT_PATH = "/inntekt"
@KtorExperimentalAPI
fun Route.inntektRoute(inntekt: InntektskomponentClient) {
   post(INNTEKT_PATH) {
      val params = call.receiveParameters()
      val inntekter = inntekt.hentInntektsliste(
         params.getOrFail("fnr"),
         YearMonth.parse(params.getOrFail("fom")),
         YearMonth.parse(params.getOrFail("tom")),
         call.callId!!
      )
      when (inntekter) {
         is Inntekter -> call.respond(HttpStatusCode.OK, inntekter.toJson())
         is Feil -> call.respond(HttpStatusCode.fromValue(inntekter.kode), inntekter.beskrivelse)
      }
   }
}
