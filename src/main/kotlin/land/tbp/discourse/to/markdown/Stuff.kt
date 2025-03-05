package land.tbp.discourse.to.markdown

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addResourceSource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds

object Dump {
    val Categories = Path("datadump/Categories.json")
    val CategoryTopics = Path("datadump/CategoryTopics.json")
    val TopicInfo = Path("datadump/TopicInfo.json")
    val TopicPosts = Path("datadump/TopicPosts.json")
}


private const val DISCOURSE_API_KEY = "Api-Key"
private const val DISCOURSE_API_USERNAME = "Api-Username"
private const val DISCOURSE_URL = "https://discourse.tbp.land"

val client = HttpClient(CIO) {
    install(Logging) {
        level = LogLevel.NONE
        //        level = LogLevel.INFO
        sanitizeHeader { it in listOf(DISCOURSE_API_KEY, DISCOURSE_API_USERNAME) }
    }
    install(ContentNegotiation) {
            json
    }
    install(HttpRequestRetry) {
        maxRetries = 1000
        retryIf { _, httpResponse ->
            httpResponse.status == HttpStatusCode.TooManyRequests
                || httpResponse.status == HttpStatusCode.NotFound
        }
        this.retryOnException(1000, retryOnTimeout = true)
        exponentialDelay(maxDelayMs = 10.seconds.inWholeMilliseconds, base = 1.1)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 5.seconds.inWholeMilliseconds
        connectTimeoutMillis = 5.seconds.inWholeMilliseconds
        socketTimeoutMillis = 5.seconds.inWholeMilliseconds
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

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    prettyPrint = true
    isLenient = false
    ignoreUnknownKeys = true
    namingStrategy = JsonNamingStrategy.SnakeCase
    encodeDefaults = true
    explicitNulls = false
}

data class Configuration(
    val apiKey: String,
    val apiUsername: String,
)

@OptIn(ExperimentalHoplite::class)
val configuration = ConfigLoaderBuilder.default()
    .withExplicitSealedTypes()
    .addResourceSource("/application-private.properties")
    .build()
    .loadConfigOrThrow<Configuration>()
