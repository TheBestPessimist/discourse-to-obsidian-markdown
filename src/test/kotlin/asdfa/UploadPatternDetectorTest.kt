import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UploadPatternDetectorTest {
    @Test
    fun `detect single image pattern`() {
        val input = "![simple image](upload://abc123.jpg)"

        val matches = findUploadPatterns(input)

        assertThat(matches).hasSize(1)
        assertThat(matches[0].fullMatch).isEqualTo("![simple image](upload://abc123.jpg)")
        assertThat(matches[0].altText).isEqualTo("simple image")
        assertThat(matches[0].uploadId).isEqualTo("abc123.jpg")
    }

    @Test
    fun `detect multiple image patterns`() {
        val input = """
                First image: ![image1](upload://abc123.jpg)
                Second image: ![image2](upload://def456.png)
            """.trimIndent()

        val matches = findUploadPatterns(input)

        assertThat(matches).hasSize(2)
        assertThat(matches[0].uploadId).isEqualTo("abc123.jpg")
        assertThat(matches[1].uploadId).isEqualTo("def456.png")
    }

    @Test
    fun `handle complex alt text with special characters`() {
        val input = """![Complex alt text with |, %, numbers 123, and symbols !@#$%^&*](upload://complex123.jpg)"""

        val matches = findUploadPatterns(input)

        assertThat(matches).hasSize(1)
        assertThat(matches[0].altText)
            .isEqualTo("Complex alt text with |, %, numbers 123, and symbols !@#$%^&*")
    }

    @Test
    fun `handle multiline image description`() {
        val input = """![This is a very
                long description that
                spans multiple lines](upload://multiline.jpg)"""

        val matches = findUploadPatterns(input)

        assertThat(matches).hasSize(1)
        assertThat(matches[0].altText).contains("This is a very", "long description")
    }

    @Test
    fun `ignore non-upload links`() {
        val input = """
                ![regular image](https://example.com/image.jpg)
                ![upload image](upload://abc123.jpg)
                ![another regular](http://example.com/pic.png)
            """.trimIndent()

        val matches = findUploadPatterns(input)

        assertThat(matches).hasSize(1)
        assertThat(matches[0].uploadId).isEqualTo("abc123.jpg")
    }

    @Test
    fun `handle empty alt text`() {
        val input = "![](upload://empty.jpg)"

        val matches = findUploadPatterns(input)

        assertThat(matches).hasSize(1)
        assertThat(matches[0].altText).isEmpty()
    }

    @Test
    fun `handle real-world example with image dimensions`() {
        val input = """
                Before click:
                ![image|690x491, 50%](upload://cT6OKwqP1BA5TcqZsmTcH4OrQGS.png)
                After click:
                ![image|565x500, 50%](upload://v0spS0ZFbb3pbGlRYmjT5CFBIQ0.jpeg)
            """.trimIndent()

        val matches = findUploadPatterns(input)

        assertThat(matches).hasSize(2)
        assertThat(matches[0].altText).isEqualTo("image|690x491, 50%")
        assertThat(matches[0].uploadId).isEqualTo("cT6OKwqP1BA5TcqZsmTcH4OrQGS.png")
        assertThat(matches[1].altText).isEqualTo("image|565x500, 50%")
        assertThat(matches[1].uploadId).isEqualTo("v0spS0ZFbb3pbGlRYmjT5CFBIQ0.jpeg")
    }

    @Test
    fun `return empty list for text without patterns`() {
        val input = """
                Just some regular text
                with [markdown](links)
                but no upload images
            """.trimIndent()

        val matches = findUploadPatterns(input)

        assertThat(matches).isEmpty()
    }

    @Test
    fun `handle special characters in upload ID`() {
        val input = "![test](upload://abc-123_456.jpg)"

        val matches = findUploadPatterns(input)

        assertThat(matches).hasSize(1)
        assertThat(matches[0].uploadId).isEqualTo("abc-123_456.jpg")
    }

    @Test
    fun `ignore malformed patterns`() {
        val input = """
                ![missing closing bracket(upload://test.jpg)
                ![proper](upload://valid.jpg)
                [missing exclamation](upload://test.jpg)
                ![missing upload prefix](regular-link.jpg)
            """.trimIndent()

        val matches = findUploadPatterns(input)

        assertThat(matches).hasSize(1)
        assertThat(matches[0].uploadId).isEqualTo("valid.jpg")
    }

    @Test
    fun `extract filename from various patterns`() {
        val testCases = listOf(
            Triple(
                "![image|690x373](upload://qyPpXP8unp0viR8SyMnaOAeg0Q3.png)",
                "image|690x373",
                "image.png"
            ),
            Triple(
                "![my awesome felsiget hand|666x200](upload://6LX9Y8dW88pEjXU4ufdLkTrkhc0.jpeg)",
                "my awesome felsiget hand|666x200",
                "my awesome felsiget hand.jpeg"
            ),
            Triple(
                "![027c4d76e2cb8fe659cc0889df8877103c8e62e6.jpg](upload://9nonJ7JqWrGNRsfCh8bvBFNJX5z.jpeg)",
                "027c4d76e2cb8fe659cc0889df8877103c8e62e6.jpg",
                "027c4d76e2cb8fe659cc0889df8877103c8e62e6.jpeg"
            ),
            Triple(
                "![youneverstoplovingsomeone-1-1.png](upload://vGnLh8KScIOro4iklzZxMI2yykB.png)",
                "youneverstoplovingsomeone-1-1.png",
                "youneverstoplovingsomeone-1-1.png"
            ),
            Triple(
                "![MediaMoneky 5 sort issue|690x373](upload://rhcwLkdGyLq9hYd2CBn2I2uerRc.gif)",
                "MediaMoneky 5 sort issue|690x373",
                "MediaMoneky 5 sort issue.gif"
            ),
            Triple(
                "![fd9a9a7f3aaf35d3b0e16c8db82ff082f86498ae](upload://biZj1mMEVcVNJE7EoQOKZi0sIkm.)",
                "fd9a9a7f3aaf35d3b0e16c8db82ff082f86498ae",
                "fd9a9a7f3aaf35d3b0e16c8db82ff082f86498ae"
            )
        )

        testCases.forEach { (input, expectedAltText, expectedFilename) ->
            val matches = findUploadPatterns(input)
            assertThat(matches).hasSize(1)
            assertThat(matches[0].altText).isEqualTo(expectedAltText)
            assertThat(matches[0].fileName).isEqualTo(expectedFilename)
        }
    }
}
