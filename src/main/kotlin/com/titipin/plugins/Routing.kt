package com.titipin

import com.titipin.modules.auth.authRoutes
import io.ktor.http.*
import io.ktor.server.application.*
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

        authRoutes()

        // ==== JASTIP DUMMY ENDPOINTS ====
        get("/jastip") {
            call.respond(listOf(
                mapOf(
                    "id"           to "1",
                    "fromLocation" to "Giant Dinoyo",
                    "toLocation"   to "Kampus UB",
                    "status"       to "ACTIVE"
                ),
                mapOf(
                    "id"           to "2",
                    "fromLocation" to "Transmart Malang",
                    "toLocation"   to "Lowokwaru",
                    "status"       to "ACTIVE"
                )
            ))
        }

        post("/jastip") {
            val body = call.receive<Map<String, String>>()
            call.respond(
                status = HttpStatusCode.Created,
                message = JastipResponse(
                    message = "Jastip berhasil dibuat",
                    data = body
                )
            )
        }

        get("/jastip/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ID tidak boleh kosong")
                )

            call.respond(mapOf(
                "id"           to id,
                "fromLocation" to "Giant Dinoyo",
                "toLocation"   to "Kampus UB",
                "status"       to "ACTIVE"
            ))
        }

        get("/jastip/slow") {
            delay(2000)
            call.respond(
                mapOf(
                    "message" to "Ini lambat tapi ga ngeblock server!",
                    "data" to "selesai setelah 2 detik"
                )
            )
        }

        get("/jastip/db-simulation") {
            val result = withContext(Dispatchers.IO) {
                delay(500)
                "data dari simulasi DB"
            }
            call.respond(mapOf("data" to result))
        }
    }
}