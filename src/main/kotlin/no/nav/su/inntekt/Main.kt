package no.nav.su.inntekt

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.concurrent.TimeUnit

fun Application.inntekt(env: Environment = Environment()) {
   routing {
      get("/isalive") {
         call.respond("ALIVE")
      }
      get("/isready") {
         call.respond("READY")
      }
      get("/inntekt") {
         call.respond("A million dollars")
      }
   }
}

fun main() {
   val app = embeddedServer(Netty, 8080) {
      inntekt()
   }.start(false)

   Runtime.getRuntime().addShutdownHook(Thread {
      app.stop(5, 60, TimeUnit.SECONDS)
   })
}
