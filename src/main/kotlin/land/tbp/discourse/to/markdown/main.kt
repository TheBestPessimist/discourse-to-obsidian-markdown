package land.tbp.discourse.to.markdown

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import io.ktor.client.*
import io.ktor.client.call.*
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


private const val DISCOURSE_API_KEY = "Api-Key"
private const val DISCOURSE_API_USERNAME = "Api-Username"
private const val DISCOURSE_URL = "https://discourse.tbp.land"

@OptIn(ExperimentalSerializationApi::class)
val objectMapper = Json {
    prettyPrint = true
    isLenient = false
    ignoreUnknownKeys = true
    namingStrategy = JsonNamingStrategy.SnakeCase
    encodeDefaults = true
    this.explicitNulls = false
}

val client = HttpClient(CIO) {
    install(Logging) {
        level = LogLevel.INFO
        this.sanitizeHeader() { it in listOf(DISCOURSE_API_KEY, DISCOURSE_API_USERNAME) }
    }
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
    val categoriesResponse = discourseRequest(client, "categories").body<CategoriesResponse>()
//    println(categoriesResponse)

    val indent = "    "

    categoriesResponse.categoryList.categories.forEach { category ->
        println(category)
        val topicsResponse = discourseRequest(client, "c", category.slug, category.id.toString()).body<CategoryTopicsResponse>()
        println(topicsResponse.topicList.moreTopicsUrl)
        topicsResponse.topicList.topics.forEach { topic ->
            println(indent + topic)
        }

        val topicsResponse2 = discourseRequest(client, topicsResponse.topicList.moreTopicsUrl!!).body<CategoryTopicsResponse>()
        println(topicsResponse2.topicList.moreTopicsUrl)
        topicsResponse2.topicList.topics.forEach { topic ->
            println(indent + topic)
        }
        return
    }
}

suspend fun discourseRequest(client: HttpClient, vararg urls: String): HttpResponse {
    return client.get(DISCOURSE_URL) {
        url {
            appendEncodedPathSegments(*urls)
        }
        header(HttpHeaders.ContentType, "application/json")
        header(HttpHeaders.Accept, "application/json")
        header(DISCOURSE_API_KEY, configuration.apiKey)
        header(DISCOURSE_API_USERNAME, configuration.apiUsername)
    }
}
