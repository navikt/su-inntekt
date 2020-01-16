package no.nav.su.inntekt.sts

import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpGet
import no.nav.su.inntekt.sts.StsToken.Companion.isValid
import org.json.JSONObject
import java.time.LocalDateTime

class STS(
   private val baseUrl: String = "http://security-token-service",
   private val username: String,
   private val password: String
) {
   private var stsToken: StsToken? = null

   fun token(): String {
      if (!isValid(stsToken)) {
         val (_, _, result) = "$baseUrl/rest/v1/sts/token?grant_type=client_credentials&scope=openid".httpGet()
            .authentication().basic(username, password)
            .header(mapOf("Accept" to "application/json"))
            .response()

         stsToken = StsToken(String(result.get()));
      }
      return stsToken!!.accessToken
   }
}

class StsToken(
   private val source: String
) {
   private val json: JSONObject = JSONObject(source)
   val accessToken: String = json.getString("access_token")
   private val expiresIn: Int = json.getInt("expires_in")
   val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expiresIn - 20L)

   companion object {
      fun isValid(token: StsToken?): Boolean {
         return when (token) {
            null -> false
            else -> !isExpired(token)
         }
      }

      private fun isExpired(token: StsToken) = token.expirationTime.isBefore(LocalDateTime.now())
   }
}
