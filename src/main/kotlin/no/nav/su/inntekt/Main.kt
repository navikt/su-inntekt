package no.nav.su.inntekt

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.concurrent.TimeUnit


fun main() {
   val app = embeddedServer(Netty, 8080) {
      inntekt()
   }.start(false)

   Runtime.getRuntime().addShutdownHook(Thread {
      app.stop(5, 60, TimeUnit.SECONDS)
   })
}
