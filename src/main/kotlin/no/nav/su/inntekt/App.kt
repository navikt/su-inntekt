package no.nav.su.inntekt

import com.auth0.jwk.JwkProviderBuilder
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import no.nav.su.inntekt.nais.nais
import org.json.JSONObject
import java.net.URL

const val INNTEKT_PATH = "/inntekt"

fun Application.inntekt(env: Environment = Environment()) {

   val jwkConfig = getJWKConfig(env.AZURE_WELLKNOWN_URL)
   val jwkProvider = JwkProviderBuilder(URL(jwkConfig.getString("jwks_uri"))).build()

   install(Authentication) {
      jwt {
         verifier(jwkProvider, jwkConfig.getString("issuer"))
         validate { credentials ->
            val groupsClaim = credentials.payload.getClaim("groups").asList(String::class.java)
            if (env.AZURE_REQUIRED_GROUP in groupsClaim && env.AZURE_CLIENT_ID in credentials.payload.audience) {
               JWTPrincipal(credentials.payload)
            } else {
               logInvalidCredentials(credentials)
               null
            }
         }
      }
   }

   routing {
      authenticate {
         get(INNTEKT_PATH) {
            call.respond("A million dollars")
         }
      }
      nais()
   }
}

private fun Application.logInvalidCredentials(credentials: JWTCredential) {
   log.info(
      "${credentials.payload.getClaim("NAVident").asString()} with audience ${credentials.payload.audience} " +
         "is not authorized to use this app, denying access"
   )
}

private fun getJWKConfig(wellKnownUrl: String): JSONObject {
   val (_, response, result) = wellKnownUrl.httpGet().responseJson()
   if (response.statusCode != HttpStatusCode.OK.value) {
      throw RuntimeException("Could not get JWK config from provider")
   } else {
      return result.get().obj()
   }
}
