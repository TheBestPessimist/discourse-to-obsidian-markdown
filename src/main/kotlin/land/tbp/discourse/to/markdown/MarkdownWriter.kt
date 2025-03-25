package land.tbp.discourse.to.markdown

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.ZoneId

private val notesPath = "D:/all/notes/discourse/"
private const val dumpPath = "D:/all/work/discourse-to-markdown/datadump/uploads/"


fun migrateFilesAndUploads(topics: List<Topic>): Unit {
    // check how many duplicate file names we have. this might lead me to implement some sort of logic for renaming files
    topics
        .flatMap { it.posts }
        .flatMap { it.uploadUrls }
        .map { it.fileName }
        .also { println(it.count())}
        .distinct()
        .also { println(it.count())}


    // // copy the attachments from my discourse posts to the vault
    // topics
    //     .flatMap { it.posts }
    //     .flatMap { it.uploadUrls }
    //     .forEach { upload ->
    //         // Files.copy(, Paths.get(notesPath, "attachments", upload.filena))
    //     }
    //
    // // create every discourse topic to the vault
    // topics.forEach { topic ->
    //     writeTopicToMarkdownFile(topic)
    // }

}





fun writeTopicToMarkdownFile(topic: Topic) {
    val frontmatter = with(topic) {
        buildString {
            appendLine("---")
            appendLine("category: $categoryName")
            appendLine("discourseTags:"); appendLine(yamlList(tags, "\"[[", "]]\""))
            appendLine("title: $title")
            appendLine("slug: $slug")
            appendLine("fullUrl:"); appendLine(yamlList(fullUrls))
            appendLine("createdAt: $createdAt")
            appendLine("created: ${createdAt.atZone(ZoneId.of("UTC")).toLocalDate()}")
            appendLine("---")
            appendLine()
        }
    }

    val posts = topic.posts.joinToString(separator = "\n${"-".repeat(30)}\n\n", transform = ::createPostMarkdown)

    write(topic, frontmatter + posts)
}


fun createPostMarkdown(p: Post): String {
    var text = p.rawMarkdown
    for (url in p.uploadUrls) {
        val fullPathOnDisk = Paths.get("$dumpPath${url.uploadIdNameOnDisk}")
        text = text.replace(url.fullMatch, """![${url.fileName}|500](<file:///${fullPathOnDisk}>)""".trimIndent())
    }
    return text
}

private fun write(topic: Topic, frontmatter: String) {
    val path = Paths.get(notesPath, "${topic.title}.md")
    println("Writing $path")
    Files.createDirectories(Paths.get(notesPath))
    Files.writeString(path, frontmatter, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}

private fun yamlList(l: List<String>, beforeIt: String = "", afterIt: String = ""): String = l.joinToString(separator = "\n") { """    - $beforeIt$it$afterIt""" }
