package no.nav.su.inntekt

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.callId
import io.ktor.features.generate
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import no.nav.su.inntekt.nais.nais
import no.nav.su.inntekt.sts.STS
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.event.Level
import java.net.URL
import java.time.YearMonth

const val INNTEKT_PATH = "/inntekt"
private val sikkerLogg = LoggerFactory.getLogger("sikkerLogg")

val collectorRegistry = CollectorRegistry.defaultRegistry

@KtorExperimentalAPI
fun Application.suinntekt(
   jwkConfig: JSONObject = getJWKConfig(fromEnvironment("azure.wellKnownUrl")),
   jwkProvider: JwkProvider = JwkProviderBuilder(URL(jwkConfig.getString("jwks_uri"))).build(),
   sts: STS = STS(username = fromEnvironment("sts.username"), password = fromEnvironment("sts.password")),
   inntekt: Inntektskomponent = Inntektskomponent(baseUrl = fromEnvironment("inntektUrl"), stsRestClient = sts)
) {
   install(Authentication) {
      jwt {
         verifier(jwkProvider, jwkConfig.getString("issuer"))
         validate { credentials ->
            val groupsClaim = credentials.payload.getClaim("groups").asList(String::class.java)
            if (fromEnvironment("azure.requiredGroup") in groupsClaim && fromEnvironment("azure.clientId") in credentials.payload.audience) {
               JWTPrincipal(credentials.payload)
            } else {
               logInvalidCredentials(credentials)
               null
            }
         }
      }
   }

   install(MicrometerMetrics) {
      registry = PrometheusMeterRegistry(
         PrometheusConfig.DEFAULT,
         collectorRegistry,
         Clock.SYSTEM
      )
      meterBinders = kotlin.collections.listOf(
         ClassLoaderMetrics(),
         JvmMemoryMetrics(),
         JvmGcMetrics(),
         ProcessorMetrics(),
         JvmThreadMetrics(),
         LogbackMetrics()
      )
   }

   routing {
      authenticate {
         install(CallId) {
            header(HttpHeaders.XRequestId)
            generate(17)
         }
         install(CallLogging) {
            level = Level.INFO
            intercept(ApplicationCallPipeline.Monitoring) {
               MDC.put(HttpHeaders.XRequestId, call.callId)
            }
         }
         post(INNTEKT_PATH) {
            val params = call.receiveParameters()
            sikkerLogg.info("${call.authentication.principal} trying to look up something ($params)")
            val inntekter = inntekt.hentInntektsliste(
               params.getOrFail("fnr"),
               YearMonth.parse(params.getOrFail("fom")),
               YearMonth.parse(params.getOrFail("tom")),
               call.callId!!
            )
            call.respond(HttpStatusCode.OK, inntekter.toJson())
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

@KtorExperimentalAPI
fun Application.fromEnvironment(path: String): String = environment.config.property(path).getString()

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

