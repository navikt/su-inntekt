package no.nav.su.inntekt.sts

import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpGet
import no.nav.su.inntekt.sts.STSToken.Companion.isValid
import org.json.JSONObject
import java.time.LocalDateTime

class STS(
   private val baseUrl: String = "http://security-token-service",
   private val username: String,
   private val password: String
) {
   private var token: STSToken? = null

   fun token(): String {
      if (!isValid(token)) {
         val (_, _, result) = "$baseUrl/rest/v1/sts/token?grant_type=client_credentials&scope=openid".httpGet()
            .authentication().basic(username, password)
            .header(mapOf("Accept" to "application/json"))
            .response()

         token = STSToken(String(result.get()));
      }
      return token!!.accessToken
   }
}

class STSToken(
   source: String
) {
   private val json: JSONObject = JSONObject(source)
   val accessToken: String = json.getString("access_token")
   private val expiresIn: Int = json.getInt("expires_in")
   private val expiryTime: LocalDateTime = LocalDateTime.now().plusSeconds(expiresIn - 20L)

   companion object {
      fun isValid(token: STSToken?): Boolean {
         return when (token) {
            null -> false
            else -> !isExpired(token)
         }
      }

      private fun isExpired(token: STSToken) = token.expiryTime.isBefore(LocalDateTime.now())
   }
}
