package com.titipin.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val dotenv      = dotenv { ignoreIfMissing = true }
    val jwtSecret   = dotenv["JWT_SECRET"]
    val jwtIssuer   = dotenv["JWT_ISSUER"]
    val jwtAudience = dotenv["JWT_AUDIENCE"]
    authentication {
        jwt("auth-jwt") {
            realm = "Titip.in API"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience))
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}
