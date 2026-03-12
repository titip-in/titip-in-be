package com.titipin.modules.wanted

import com.titipin.shared.ApiErrorCodes
import com.titipin.shared.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.wantedRoutes() {
    val repository = WantedRepository()
    val service    = WantedService(repository)

    route("/wanted") {

        authenticate("auth-jwt") {

            get {
                val list = service.getAllOpen()
                call.respond(HttpStatusCode.OK, ApiResponse.success(list))
            }

            post {
                try {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse.error<Unit>(ApiErrorCodes.UNAUTHORIZED, "Unauthorized"))

                    val request = call.receive<CreateWantedRequest>()
                    val result  = service.create(userId, request)

                    call.respond(HttpStatusCode.Created, ApiResponse.success(data = result, message = "Pencarian barang berhasil dibuat"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, e.message ?: "Invalid request"))
                }
            }

            put("/{id}/fulfill") {
                try {
                    val fulfillerId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()
                        ?: return@put call.respond(HttpStatusCode.Unauthorized, ApiResponse.error<Unit>(ApiErrorCodes.UNAUTHORIZED, "Unauthorized"))

                    val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>(ApiErrorCodes.BAD_REQUEST, "ID kosong"))

                    val result = service.fulfillWanted(id, fulfillerId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(data = result, message = "Berhasil menawarkan barang ke pencari!"))
                } catch (e: IllegalStateException) {
                    when (e.message) {
                        ApiErrorCodes.NOT_FOUND -> call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>(ApiErrorCodes.NOT_FOUND, "Pencarian tidak ditemukan"))
                        ApiErrorCodes.FORBIDDEN -> call.respond(HttpStatusCode.Forbidden, ApiResponse.error<Unit>(ApiErrorCodes.FORBIDDEN, "Tidak bisa menawarkan barang ke diri sendiri"))
                        ApiErrorCodes.ITEM_NOT_AVAILABLE -> call.respond(HttpStatusCode.Conflict, ApiResponse.error<Unit>(ApiErrorCodes.CONFLICT, "Pencarian ini sudah dipenuhi orang lain"))
                        else -> call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Unit>(ApiErrorCodes.SERVER_ERROR, "Terjadi kesalahan"))
                    }
                }
            }
        }
    }
}