package land.tbp.discourse.to.markdown

import kotlinx.serialization.Serializable
import land.tbp.discourse.to.markdown.processing.base62Decode
import java.time.Instant

// This is the thing i will write in a file
@Serializable
data class Topic(
    val categoryName: String,
    val tags: List<String>,
    val title: String,
    val slug: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now(),
    val posts: List<Post>,
    val fullUrls: List<String>,
)


@Serializable
data class Post(
    val rawMarkdown: String,
    val id: Int,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now(),
    val uploadUrls: List<UploadMatch>,
) {
    val markdownWithImages: String by lazy {
        for (upload in uploadUrls) {
            rawMarkdown.replace(upload.fullMatch, uploadUrls)
        }
        ""
    }
}

@Serializable
data class UploadMatch(
    val fullMatch: String,
    val altText: String,
    val uploadId: String,
) {
    val fileName: String by lazy {
        // Get the extension from [uploadId]
        val extension = when {
            uploadId.endsWith(".") -> "" // Some uploadIds end with . and have no extension
            uploadId.contains(".") -> uploadId.substringAfterLast(".")
            else -> "" // No extension found
        }

        // Extract the base filename from [altText]
        val baseFileName = when {
            // If altText already contains a filename with extension, use it without the extension
            altText.contains(".") && !altText.contains("|") ->
                altText.substringBeforeLast(".")

            // Handle cases with dimensions (e.g., "image|690x373" or "my file|123x456")
            altText.contains("|") ->
                altText.substringBefore("|").trim()

            // If it's just a plain text without extension or dimensions, use it as is
            else -> altText.trim()
        }

        // Combine filename and extension
        if (extension.isNotEmpty()) "$baseFileName.$extension" else baseFileName
    }

    val uploadIdNameOnDisk: String by lazy {
        // Get the extension from [uploadId]
        val extension = when {
            uploadId.endsWith(".") -> "" // Some uploadIds end with . and have no extension
            uploadId.contains(".") -> uploadId.substringAfterLast(".")
            else -> "" // No extension found
        }

        val hash = when {
            uploadId.endsWith(".") -> uploadId // Some uploadIds end with . and have no extension
            uploadId.contains(".") -> uploadId.substringBeforeLast(".")
            else -> uploadId // No extension found
        }

        val uploadHashDecoded = base62Decode(hash)
        "datadump/uploads/$uploadHashDecoded.$extension"
    }
}
