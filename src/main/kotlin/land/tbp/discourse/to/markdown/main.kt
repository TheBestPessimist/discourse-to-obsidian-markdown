package land.tbp.discourse.to.markdown

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
val objectMapper = Json {
    prettyPrint = true
    isLenient = false
    ignoreUnknownKeys = true
    namingStrategy = JsonNamingStrategy.SnakeCase
}

val client = HttpClient(CIO) {
    install(Logging)
    install(ContentNegotiation) {
        json(objectMapper)
    }
}

data class Configuration(
    val apiKey: String,
    val apiUsername: String,
)

val configuration = ConfigLoaderBuilder.default()
    .addResourceSource("/application-private.properties")
    .build()
    .loadConfigOrThrow<Configuration>()

suspend fun main() {
    val response: HttpResponse = client.get("https://discourse.tbp.land/categories") {
        header(HttpHeaders.ContentType, "application/json")
        header(HttpHeaders.Accept, "application/json")
        header("Api-Key", configuration.apiKey)
        header("Api-Username", configuration.apiUsername)
    }
    println(response.bodyAsText())
    println(objectMapper.decodeFromString<CategoriesResponse>(response.bodyAsText()))
}
