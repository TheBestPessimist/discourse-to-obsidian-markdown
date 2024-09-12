package land.tbp.discourse.to.markdown

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Constants.indent
import com.sksamuel.hoplite.addResourceSource
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.Path
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds


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
    explicitNulls = false
}

val client = HttpClient(CIO) {
    install(Logging) {
        level = LogLevel.NONE
//        level = LogLevel.INFO
        sanitizeHeader { it in listOf(DISCOURSE_API_KEY, DISCOURSE_API_USERNAME) }
    }
    install(ContentNegotiation) {
        json(objectMapper)
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

data class Configuration(
    val apiKey: String,
    val apiUsername: String,
)

val configuration = ConfigLoaderBuilder.default()
    .addResourceSource("/application-private.properties")
    .build()
    .loadConfigOrThrow<Configuration>()

val topicCount = AtomicInteger()
val postCount = AtomicInteger()

val allTopics: MutableList<Topic> = Collections.synchronizedList(mutableListOf<Topic>())

fun main() {
    runBlocking {
        val categoriesResponse = discourseRequest(client, "categories").body<CategoriesResponse>()
        val indent = "    "



        for (category in categoriesResponse.categoryList.categories) {
//            if (category.name!="Blog")
//            if (category.name!="Discourse Testing")
//            if (category.name!="Site Feedback/Meta")
            if (category.name!="Staff")
//            if (category.name!="Lounge")
//            if (category.name!="A Shadow of the Day...")
//            if (category.name!="The Mighty Nahsuc's Song Of The Day")
//            if (category.name!="pokambrian category")
                continue
            launch {
                println(category)
                val topicInfos = getTopicInfos(client, "c", category.slug, category.id.toString())
                topicInfos.forEach { topicInfo ->
                    launch {
                        println(indent.repeat(1) + topicInfo)
                        val topicPostInfos = getTopicPostInfos(client, "t", topicInfo.id.toString())
                        topicCount.incrementAndGet()

                        val posts = coroutineScope {
                            topicPostInfos.postStream.stream.map { postId ->
                                async {
                                    val rawPost = discourseRequest(client, "posts", postId.toString()).body<PostResponse>().raw
                                    Post(rawPost, postId)
                                }
                            }.awaitAll()
                        }
                        val topic = Topic(category.name, emptyList(), topicInfo.title, topicInfo.slug, topicInfo.createdAt, posts.sortedBy { it.id }, emptyList())
                        allTopics.add(topic)
                    }
                }
            }
        }
    }
    println("ZZZZZZZZZZZZZZZZ" + topicCount.get())
    println("ZZZZZZZZZZZZZZZZ" + postCount.get())
    println(allTopics.size)
    val t = allTopics.sortedWith(compareBy({ it.categoryName }, { it.title }))
    Files.writeString(Path("./zzzzzzzz.txt"), t.joinToString("\n".repeat(10)))
}

private suspend fun getTopicPostInfos(client: HttpClient, vararg urls: String): TopicPostsResponse {
    println(indent.repeat(1) + urls.joinToString())
    val ti = discourseRequest(client, *urls).body<TopicPostsResponse>()
    println(indent.repeat(2) + ti)
    postCount.addAndGet(ti.postStream.stream.count())
    return ti
}

private suspend fun getTopicInfos(client: HttpClient, vararg urls: String): List<TopicInfo> {
    if (urls.isEmpty()) return emptyList()

    println(indent + urls.joinToString())
    val topicsResponse = discourseRequest(client, *urls).body<CategoryTopicsResponse>()
    return topicsResponse.topicList.topics + (topicsResponse.topicList.moreTopicsUrl?.let { getTopicInfos(client, it) } ?: emptyList())
}

private suspend fun discourseRequest(client: HttpClient, vararg urls: String): HttpResponse {
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
