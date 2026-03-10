package com.titipin.modules.preloved

import com.titipin.shared.ApiErrorCodes
import com.titipin.shared.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.prelovedRoutes() {
    val repository = PrelovedRepository()
    val service    = PrelovedService(repository)

    route("/preloved") {

        // GET /preloved — public
        get {
            val listings = service.getAll()
            call.respond(
                HttpStatusCode.OK,
                ApiResponse.success<List<PrelovedDto>>(data = listings)
            )
        }

        // GET /preloved/{id} — public
        get("/{id}") {
            try {
                val id     = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, "ID tidak boleh kosong")
                )
                val result = service.getById(id)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success<PrelovedDto>(data = result)
                )
            } catch (e: IllegalStateException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse.error<Unit>(ApiErrorCodes.NOT_FOUND, "Item tidak ditemukan")
                )
            }
        }

        // POST & DELETE — protected
        authenticate("auth-jwt") {

            // POST /preloved
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId    = principal?.payload?.getClaim("id")?.asString()
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>(ApiErrorCodes.UNAUTHORIZED, "Unauthorized")
                        )

                    val request = call.receive<CreatePrelovedRequest>()
                    val result  = service.create(userId, request)
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse.success<PrelovedDto>(
                            data    = result,
                            message = "Item preloved berhasil dibuat"
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, e.message ?: "Request tidak valid")
                    )
                }
            }

            // DELETE /preloved/{id}
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
                        ApiResponse.success<Unit>(message = "Item berhasil dihapus")
                    )
                } catch (e: IllegalStateException) {
                    when (e.message) {
                        ApiErrorCodes.NOT_FOUND -> call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Unit>(ApiErrorCodes.NOT_FOUND, "Item tidak ditemukan")
                        )
                        ApiErrorCodes.INSUFFICIENT_PERMISSION -> call.respond(
                            HttpStatusCode.Forbidden,
                            ApiResponse.error<Unit>(ApiErrorCodes.INSUFFICIENT_PERMISSION, "Bukan item kamu")
                        )
                        else -> call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponse.error<Unit>(ApiErrorCodes.SERVER_ERROR, "Terjadi kesalahan")
                        )
                    }
                }
            }

            // PUT /preloved/{id}
            put("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId    = principal?.payload?.getClaim("id")?.asString()
                        ?: return@put call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>(ApiErrorCodes.UNAUTHORIZED, "Unauthorized")
                        )

                    val id      = call.parameters["id"]
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, "ID tidak boleh kosong")
                        )

                    val request = call.receive<UpdatePrelovedRequest>()
                    val result  = service.updateStatus(id, userId, request)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success<PrelovedDto>(
                            data    = result,
                            message = "Status item berhasil diupdate"
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, e.message ?: "Request tidak valid")
                    )
                } catch (e: IllegalStateException) {
                    when (e.message) {
                        ApiErrorCodes.NOT_FOUND -> call.respond(
                            HttpStatusCode.NotFound,
                            ApiResponse.error<Unit>(ApiErrorCodes.NOT_FOUND, "Item tidak ditemukan")
                        )
                        ApiErrorCodes.INSUFFICIENT_PERMISSION -> call.respond(
                            HttpStatusCode.Forbidden,
                            ApiResponse.error<Unit>(ApiErrorCodes.INSUFFICIENT_PERMISSION, "Bukan item kamu")
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