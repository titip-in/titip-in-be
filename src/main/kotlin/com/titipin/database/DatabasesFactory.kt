package com.titipin

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.titipin.database.tables.JastipTable
import com.titipin.database.tables.PrelovedTable
import com.titipin.database.tables.UsersTable
import com.titipin.database.tables.WantedTable
import com.titipin.database.tables.JastipRequestTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import io.github.flaxoos.ktor.server.plugins.ratelimiter.*
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.*
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import java.sql.DriverManager
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.*

fun Application.configureDatabases() {

    val dotenv = dotenv {
        ignoreIfMissing = true
    }
    val dbUrl = dotenv["DB_URL"]
    val dbUser = dotenv["DB_USER"]
    val dbPassword = dotenv["DB_PASSWORD"]

    // HikariCP connection pool
    val config = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPassword
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
        minimumIdle = 2
        idleTimeout = 300000
        connectionTimeout = 30000
    }

    val dataSource = HikariDataSource(config)

    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(
            UsersTable,
            JastipTable,
            PrelovedTable,
            JastipRequestTable,
            WantedTable
        )
    }
    log.info("Database connected successfully!")
}
