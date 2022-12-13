package fr.northborders

import fr.northborders.models.ApiResponse
import fr.northborders.plugins.configureRouting
import fr.northborders.repository.HeroRepositoryImpl
import fr.northborders.repository.NEXT_PAGE_KEY
import fr.northborders.repository.PREVIOUS_PAGE_KEY
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `access root endpoint, assert correct information`() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status)
            assertEquals(
                expected = "Welcome to Boruto Api",
                actual = bodyAsText())
        }
    }

    @Test
    fun `access all heroes endpoint, assert correct information`() = testApplication {
        application {
            configureRouting()
        }
        val heroRepository = HeroRepositoryImpl()
        client.get("/boruto/heroes").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status)
            val expected = ApiResponse(
                success = true,
                message = "ok",
                prevPage = null,
                nextPage = 2,
                heroes = heroRepository.page1
            )
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
            assertEquals(
                expected = expected,
                actual = actual)
        }
    }

    @Test
    fun `access all heroes endpoint, query all pages, assert correct information`() = testApplication {
        application {
            configureRouting()
        }
        val heroRepository = HeroRepositoryImpl()
        val pages = 1..5
        val heroes = listOf(
            heroRepository.page1,
            heroRepository.page2,
            heroRepository.page3,
            heroRepository.page4,
            heroRepository.page5
        )
        pages.forEach {page ->
            client.get("/boruto/heroes?page=$page").apply {
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = status)
                val expected = ApiResponse(
                    success = true,
                    message = "ok",
                    prevPage = calculatePage(page = page)["prevPage"],
                    nextPage = calculatePage(page = page)["nextPage"],
                    heroes = heroes[page - 1]
                )
                val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
                assertEquals(
                    expected = expected,
                    actual = actual)
            }
        }
    }

    @Test
    fun `access all heroes endpoint, query non existing page, assert error`() = testApplication {
        application {
            configureRouting()
        }
        client.get("/boruto/heroes?page=6").apply {
            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = status)
            val expected = ApiResponse(
                success = false,
                message = "Heroes not found."
            )
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
            assertEquals(
                expected = expected,
                actual = actual)
        }
    }

    @Test
    fun `access all heroes endpoint, query invalid page number, assert error`() = testApplication {
        application {
            configureRouting()
        }
        client.get("/boruto/heroes?page=INVALID").apply {
            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = status)
            val expected = ApiResponse(
                success = false,
                message = "Only numbers allowed."
            )
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
            assertEquals(
                expected = expected,
                actual = actual)
        }
    }

    @Test
    fun `access search heroes endpoint, query hero name, assert single hero`() = testApplication {
        application {
            configureRouting()
        }
        client.get("/boruto/heroes/search?name=sas").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status)
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).heroes.size
            assertEquals(
                expected = 1,
                actual = actual)
        }
    }

    @Test
    fun `access search heroes endpoint, query hero name, assert multiple heroes result`() = testApplication {
        application {
            configureRouting()
        }
        client.get("/boruto/heroes/search?name=sa").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status)
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).heroes.size
            assertEquals(
                expected = 3,
                actual = actual)
        }
    }

    @Test
    fun `access search heroes endpoint, query empty text, assert empty list as a result`() = testApplication {
        application {
            configureRouting()
        }
        client.get("/boruto/heroes/search?name=").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status)
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).heroes
            assertEquals(
                expected = emptyList(),
                actual = actual)
        }
    }

    @Test
    fun `access search heroes endpoint, query non existing hero, assert empty list as a result`() = testApplication {
        application {
            configureRouting()
        }
        client.get("/boruto/heroes/search?name=UNKNOWN").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status)
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText()).heroes
            assertEquals(
                expected = emptyList(),
                actual = actual)
        }
    }

    @Test
    fun `access non existing endpoint, assert non found`() = testApplication {
        application {
            configureRouting()
        }
        client.get("/boruto/heroes/unknown").apply {
            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = status)
        }
    }

    private fun calculatePage(page: Int): Map<String, Int?> {
        var prevPage: Int? = page
        var nextPage: Int? = page
        if (page in 1..4) {
            nextPage = nextPage?.plus(1)
        }
        if (page in 2..5) {
            prevPage = prevPage?.minus(1)
        }
        if (page == 1) {
            prevPage = null
        }
        if (page == 5) {
            nextPage = null
        }
        return mapOf(PREVIOUS_PAGE_KEY to prevPage, NEXT_PAGE_KEY to nextPage)
    }
}