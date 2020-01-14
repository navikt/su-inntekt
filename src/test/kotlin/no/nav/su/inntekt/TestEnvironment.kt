package no.nav.su.inntekt

import com.github.tomakehurst.wiremock.WireMockServer

const val AZURE_GROUP_ID = "su-gruppa"
const val AZURE_CLIENT_ID = "clientId"
const val AZURE_ISSUER = "azure"

fun testEnvironment(wireMockServer: WireMockServer) = Environment(
   AZURE_WELLKNOWN_URL = "${wireMockServer.baseUrl()}/config",
   AZURE_CLIENT_ID = AZURE_CLIENT_ID,
   AZURE_REQUIRED_GROUP = AZURE_GROUP_ID
)
