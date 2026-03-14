package com.titipin

import com.titipin.modules.auth.authRoutes
import com.titipin.modules.jastip.jastipRoutes
import com.titipin.modules.preloved.prelovedRoutes
import com.titipin.modules.request.requestRoutes
import com.titipin.modules.wanted.wantedRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

//routing
@Serializable
data class JastipResponse(
    val message: String,
    val data: Map<String, String>
)

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf(
                "message" to "Welcome to Titip.in API",
                "version" to "0.0.1"
            ))
        }

        // health check
        get("/health") {
            call.respond(mapOf(
                "status" to "OK",
                "app"    to "Titip.in API",
                "version" to "0.0.1"
            ))
        }

        authRoutes() // auth/register auth/login
        jastipRoutes() // jastip /jastip{id}
        prelovedRoutes() // /preloved /preloved/{id}
        wantedRoutes() // /wanted /wanted/{id}
        requestRoutes() // /request /request/{id}
    }
}