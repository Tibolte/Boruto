package fr.northborders.plugins

import fr.northborders.routes.getAllHeroes
import fr.northborders.routes.root
import fr.northborders.routes.searchHeroes
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    routing {
        root()
        getAllHeroes()
        searchHeroes()
    }
}
