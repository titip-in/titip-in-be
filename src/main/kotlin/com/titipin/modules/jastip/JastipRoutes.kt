package com.titipin.modules.jastip

import com.titipin.shared.ApiErrorCodes
import com.titipin.shared.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.jastipRoutes() {
    val repository = JastipRepository()
    val service    = JastipService(repository)

    route("/jastip") {

        // GET /jastip — public, siapapun bisa lihat
        get {
            val listings = service.getAll()
            call.respond(
                HttpStatusCode.OK,
                ApiResponse.success<List<JastipDto>>(
                    data    = listings,
                    message = "OK"
                )
            )
        }

        // GET /jastip/{id} — public
        get("/{id}") {
            try {
                val id     = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, "ID tidak boleh kosong")
                )
                val result = service.getById(id)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success<JastipDto>(data = result)
                )
            } catch (e: IllegalStateException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse.error<Unit>(ApiErrorCodes.NOT_FOUND, "Jastip tidak ditemukan")
                )
            }
        }

        // POST & DELETE — protected, harus login
        authenticate("auth-jwt") {

            // POST /jastip — buat listing baru
            post {
                try {
                    // ambil userId dari JWT token
                    val principal = call.principal<JWTPrincipal>()
                    val userId    = principal?.payload?.getClaim("id")?.asString()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>(ApiErrorCodes.UNAUTHORIZED, "Unauthorized")
                        )

                    val request = call.receive<CreateJastipRequest>()
                    val result  = service.create(userId, request)
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse.success<JastipDto>(
                            data    = result,
                            message = "Jastip berhasil dibuat"
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, e.message ?: "Request tidak valid")
                    )
                }
            }

            // DELETE /jastip/{id}
            delete("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId    = principal?.payload?.getClaim("id")?.asString()
                        ?: return@delete call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>(ApiErrorCodes.UNAUTHORIZED, "Unauthorized")
                        )

                    val id = call.parameters["id"]
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, "ID tidak boleh kosong")
                        )

                    service.delete(id, userId)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success<Unit>(message = "Jastip berhasil dihapus")
                    )
                } catch (e: IllegalStateException) {
                    when (e.message) {
                        ApiErrorCodes.NOT_FOUND -> call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Unit>(ApiErrorCodes.NOT_FOUND, "Jastip tidak ditemukan")
                        )
                        ApiErrorCodes.INSUFFICIENT_PERMISSION -> call.respond(
                            HttpStatusCode.Forbidden,
                            ApiResponse.error<Unit>(ApiErrorCodes.INSUFFICIENT_PERMISSION, "Bukan jastip kamu")
                        )
                        else -> call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponse.error<Unit>(ApiErrorCodes.SERVER_ERROR, "Terjadi kesalahan")
                        )
                    }
                }
            }
        }
    }
}