package land.tbp.discourse.to.markdown.processing

import io.seruco.encoding.base62.Base62


// this is how i convert the base62 hashes to the actual image hash url
fun main() {
    println(base62Decode("6LX9Y8dW88pEjXU4ufdLkTrkhc0").also(::println) == "2f78c1ac988dd200abdba00c2ef28678eb0650dc")
    println(base62Decode("o3t8U3Df0Nrf0D2fcmX1PEDmTfJ").also(::println) == "a89897c22b40a070068d7a395ab18e6a545197ab")
    println(base62Decode("bIHzpmIohIJFFGCqxLDlWXZbNC7").also(::println) == "a89897c22b40a070068d7a395ab18e6a545197ab")



}


@OptIn(ExperimentalStdlibApi::class)
fun base62Decode(uploadUrlHash: String): String {
    val b = Base62.createInstanceWithInvertedCharacterSet()
    val d = b.decode(uploadUrlHash.encodeToByteArray())
    val decoded = d.toHexString(HexFormat.Default)
    return decoded
}


/*
this one fails:

UploadUrl(fullMatch=![eb10d828f8f1a4fe4a33761d9ff93cd227eca623](upload://bIHzpmIohIJFFGCqxLDlWXZbNC7.),
altText=eb10d828f8f1a4fe4a33761d9ff93cd227eca623,
 uploadId=bIHzpmIohIJFFGCqxLDlWXZbNC7.)

 */
