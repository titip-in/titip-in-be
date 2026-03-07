package com.titipin.modules.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.titipin.shared.ApiErrorCodes
import io.github.cdimascio.dotenv.dotenv
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

class AuthService(private val repository: AuthRepository) {
    private val dotenv = dotenv { ignoreIfMissing = true }
    private val jwtSecret   = dotenv["JWT_SECRET"]
    private val jwtIssuer   = dotenv["JWT_ISSUER"]
    private val jwtAudience = dotenv["JWT_AUDIENCE"]

    suspend fun register(request: RegisterRequest): AuthResponse{
        require(request.name.isNotBlank())     { "Nama tidak boleh kosong" }
        require(request.email.isNotBlank())    { "Email tidak boleh kosong" }
        require(request.password.length >= 8)  { "Password minimal 8 karakter" }
        require(request.waNumber.isNotBlank()) { "Nomor WA tidak boleh kosong" }

        //existing check
        val existing = repository.findUserByEmail(request.email)
        if (existing != null) {
            throw IllegalStateException(ApiErrorCodes.EMAIL_ALREADY_EXISTS)
        }

        //hash pw
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())

        // user
        val user = repository.createUser(request,hashedPassword)

        //jwt token
        val token = generateToken(user.id, user.email)
        return AuthResponse(accessToken = token, user = user)
    }

    suspend fun login(request: LoginRequest): AuthResponse{
        require(request.email.isNotBlank())    { "Email tidak boleh kosong" }
        require(request.password.isNotBlank()) { "Password tidak boleh kosong" }

        val userRow = repository.findUserByEmail(request.email)
            ?: throw IllegalStateException(ApiErrorCodes.INVALID_CREDENTIALS)

        // 2. verifikasi password
        // BCrypt.checkpw() → compare plain text vs hash di DB
        val isPasswordValid = BCrypt.checkpw(
            request.password,
            userRow[com.titipin.database.tables.UsersTable.password]
        )

        if (!isPasswordValid) {
            throw IllegalStateException(ApiErrorCodes.INVALID_CREDENTIALS)
        }

        // 3. convert ke UserDto
        val user =  with(repository) { userRow.toUserDto()}

        // 4. generate token
        val token = generateToken(user.id, user.email)

        return AuthResponse(accessToken = token, user = user)
    }

    private fun generateToken(id: String, email: String): String {
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim("id", id)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000)) // 24 hours
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}