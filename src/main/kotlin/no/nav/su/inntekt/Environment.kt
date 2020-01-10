package no.nav.su.inntekt

data class Environment(
   val AZURE_WELLKNOWN_URL: String = getEnvVar("AZURE_WELLKNOWN_URL"),
   val AZURE_CLIENT_ID: String = getEnvVar("AZURE_CLIENT_ID"),
   val AZURE_REQUIRED_GROUP: String = getEnvVar("AZURE_REQUIRED_GROUP")
)

private fun getEnvVar(varName: String) = getOptionalEnvVar(varName) ?: throw Exception("mangler verdi for $varName")

private fun getOptionalEnvVar(varName: String): String? = System.getenv(varName)
