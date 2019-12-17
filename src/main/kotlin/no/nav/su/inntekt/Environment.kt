package no.nav.su.inntekt

data class Environment(val map: Map<String, String> = System.getenv()) {

    companion object {
        const val CORS_KEY = "ALLOW_CORS_ORIGIN"
    }

    val allowCorsOrigin: String = envVar(CORS_KEY)

    private fun envVar(key: String, defaultValue: String? = null): String {
        return map[key] ?: defaultValue ?: throw RuntimeException("Missing required variable \"$key\"")
    }

}
