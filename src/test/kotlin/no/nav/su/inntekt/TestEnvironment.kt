package no.nav.su.inntekt

import com.github.tomakehurst.wiremock.WireMockServer

const val oidcGroupUuid = "ENLANG-AZURE-OBJECT-GRUPPE-ID"
const val clientId = "clientId"
const val issuer = "azure"

fun testEnvironment(wireMockServer: WireMockServer) = Environment(
   oidcConfigUrl = "${wireMockServer.baseUrl()}/config",
   oidcClientId = clientId,
   oidcRequiredGroup = oidcGroupUuid
)
