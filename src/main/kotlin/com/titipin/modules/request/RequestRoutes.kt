package com.titipin.modules.request

import com.titipin.shared.ApiErrorCodes
import com.titipin.shared.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.requestRoutes() {
    val repository = RequestRepository()
    val service    = RequestService(repository)

    authenticate("auth-jwt") {
        route("/requests") {

            get {
                val requests = service.getAll()
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success<List<RequestDto>>(data = requests)
                )
            }

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId    = principal?.payload?.getClaim("id")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse.error<Unit>(ApiErrorCodes.UNAUTHORIZED, "Unauthorized"))

                    val request = call.receive<CreateRequestRequest>()
                    val result  = service.create(userId, request)

                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse.success<RequestDto>(data = result, message = "Request jastip berhasil dibuat")
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, e.message ?: "Request tidak valid")
                    )
                }
            }

            put("/{id}/take") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val takerId   = principal?.payload?.getClaim("id")?.asString()
                        ?: return@put call.respond(HttpStatusCode.Unauthorized, ApiResponse.error<Unit>(ApiErrorCodes.UNAUTHORIZED, "Unauthorized"))

                    val id = call.parameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, "ID tidak boleh kosong"))

                    val result = service.takeRequest(id, takerId)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success<TakeRequestResponse>(data = result, message = "Request berhasil diambil")
                    )
                } catch (e: IllegalStateException) {
                    when (e.message) {
                        ApiErrorCodes.NOT_FOUND -> call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>(ApiErrorCodes.NOT_FOUND, "Request tidak ditemukan"))
                        ApiErrorCodes.FORBIDDEN -> call.respond(HttpStatusCode.Forbidden, ApiResponse.error<Unit>(ApiErrorCodes.FORBIDDEN, "Tidak bisa mengambil request sendiri"))
                        ApiErrorCodes.ITEM_NOT_AVAILABLE -> call.respond(HttpStatusCode.Conflict, ApiResponse.error<Unit>(ApiErrorCodes.CONFLICT, "Request sudah diambil orang lain"))
                        else -> call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Unit>(ApiErrorCodes.SERVER_ERROR, "Terjadi kesalahan server"))
                    }
                }
            }
        }
    }
}