package com.titipin

import com.titipin.plugins.configureSecurity
import com.titipin.plugins.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureAdministration()
    configureRouting()
}
