package no.nav.su.inntekt

data class Environment(val map: Map<String, String> = System.getenv()) {

    private fun envVar(key: String, defaultValue: String? = null): String {
        return map[key] ?: defaultValue ?: throw RuntimeException("Missing required variable \"$key\"")
    }

}
