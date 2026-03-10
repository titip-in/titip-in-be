package com.titipin.modules.auth


import com.titipin.shared.ApiErrorCodes
import com.titipin.shared.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val repository = AuthRepository()
    val service = AuthService(repository)

    route("/auth") {

        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val result = service.register(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse.success(
                        data = result,
                        message = "Registrasi berhasil"
                    )
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.error<Unit>(
                        code = ApiErrorCodes.BAD_REQUEST,
                        message = e.message ?: "Request tidak valid"
                    )
                )
            } catch (e: IllegalStateException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponse.error<Unit>(
                        code = e.message ?: ApiErrorCodes.CONFLICT,
                        message = "Email sudah terdaftar"
                    )
                )
            }
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val result = service.login(request)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.success(
                        data = result,
                        message = "Login berhasil"
                    )
                )
            } catch (e: IllegalStateException) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse.error<Unit>(
                        code = ApiErrorCodes.INVALID_CREDENTIALS,
                        message = "Email atau password salah"
                    )
                )
            }
        }

        authenticate("auth-jwt") {

            // GET /auth/me
            get("/me") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId    = principal?.payload?.getClaim("id")?.asString()
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Unit>(ApiErrorCodes.UNAUTHORIZED, "Unauthorized")
                        )

                    val user = service.getMe(userId)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success<UserDto>(data = user)
                    )
                } catch (e: IllegalStateException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse.error<Unit>(ApiErrorCodes.NOT_FOUND, "User tidak ditemukan")
                    )
                }
            }
        }
    }
}