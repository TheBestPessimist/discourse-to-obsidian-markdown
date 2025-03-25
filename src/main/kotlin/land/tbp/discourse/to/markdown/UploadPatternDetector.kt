import land.tbp.discourse.to.markdown.UploadUrl
import java.util.regex.Pattern

// This pattern matches:
// - Starting with ![
// - Followed by any characters (non-greedy) until ]
// - Followed by (upload:// and then any characters until the first )
private val UPLOAD_PATTERN = Pattern.compile(
    """!\[([^]]*?)]\(upload://([^)\s]+)\)""",
    Pattern.MULTILINE or Pattern.DOTALL
)

/**
 * Finds all upload patterns in the given text that match the format:
 * ![any text](upload://any_text_without_spaces)
 *
 * @param text The input text to search for patterns
 * @return List of UploadMatch objects containing the full match, alt text, and upload ID
 */
fun findUploadPatterns(text: String): List<UploadUrl> {
    val matches = mutableListOf<UploadUrl>()
    val matcher = UPLOAD_PATTERN.matcher(text)

    while (matcher.find()) {
        matches.add(
            UploadUrl(
                fullMatch = matcher.group(0),
                altText = matcher.group(1),
                uploadId = matcher.group(2),
            )
        )
    }

    return matches
}
