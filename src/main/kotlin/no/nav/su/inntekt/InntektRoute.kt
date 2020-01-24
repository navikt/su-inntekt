package no.nav.su.inntekt

import io.ktor.application.call
import io.ktor.features.callId
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI

const val INNTEKT_PATH = "/inntekt"
@KtorExperimentalAPI
internal fun Route.inntektRoute(inntekt: InntektskomponentClient) {
   post(INNTEKT_PATH) {
      val callParameters = call.receiveParameters()
      val søkeresultat = when (val parametere = ParameterTranslator(callParameters).parsedParameters) {
         is FeilMedParametere -> Feil(400, parametere.melding)
         is Søkeparametere -> inntekt.hentInntektsliste(parametere, call.callId!!)
      }
      when (søkeresultat) {
         is Inntekter -> call.respond(HttpStatusCode.OK, søkeresultat.toJson())
         is Feil -> call.respond(HttpStatusCode.fromValue(søkeresultat.kode), søkeresultat.beskrivelse)
      }
   }
}
