package com.alchembright.dev.langtrackapp.data

internal object AccountLoginPolicy {
    enum class Route { LEGACY, USERNAME, FAIL }
    fun options(status: Int, enabled: Boolean?): Route = when {
        status == 404 -> Route.LEGACY // Compatibility with the pre-module server.
        status != 200 || enabled == null -> Route.FAIL
        enabled -> Route.USERNAME
        else -> Route.LEGACY
    }
    fun allowLegacyFallback(status: Int) = status == 401
}
