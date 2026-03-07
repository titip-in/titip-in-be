package com.titipin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Serializable
data class JastipResponse(
    val message: String,
    val data: Map<String, String>
)

fun Application.configureRouting() {
    routing {
        // Root endpoint
        get("/") {
            call.respond(mapOf(
                "message" to "Welcome to Titip.in API",
                "version" to "0.0.1"
            ))
        }
        // Health check — buat ngecek server nyala
        get("/health") {
            call.respond(mapOf(
                "status" to "OK",
                "app"    to "Titip.in API",
                "version" to "0.0.1"
            ))
        }

        // === JASTIP (dummy dulu, belum connect DB) ===
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
            // call.parameters["id"] → pengganti req.params.id di Express
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
            // simulasi "nunggu DB query 2 detik"
            // ini TIDAK ngeblock server — request lain tetap bisa masuk
            delay(2000)

            call.respond(
                mapOf(
                    "message" to "Ini lambat tapi ga ngeblock server!",
                    "data" to "selesai setelah 2 detik"
                )
            )
        }

        get("/jastip/db-simulation") {
            // simulasi "pergi ke jalur IO buat query DB"
            val result = withContext(Dispatchers.IO) {
                delay(500) // pura-pura query DB 500ms
                "data dari simulasi DB" // ini return value-nya
            }
            // result langsung bisa dipakai di sini
            call.respond(mapOf("data" to result))
        }
        }
}