package fr.northborders

import io.ktor.server.application.*
import fr.northborders.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureKoin()
    configureDefaultHeaders()
    configureSerialization()
    configureMonitoring()
    configureRouting()
}
