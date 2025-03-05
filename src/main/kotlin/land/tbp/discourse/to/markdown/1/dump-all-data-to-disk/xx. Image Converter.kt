package land.tbp.discourse.to.markdown.`1`.`dump-all-data-to-disk`

import io.seruco.encoding.base62.Base62


// this is how i convert the base62 hashes to the actual image hash url
@OptIn(ExperimentalStdlibApi::class)
fun main() {
    val b = Base62.createInstanceWithInvertedCharacterSet()
    val d = b.decode("6LX9Y8dW88pEjXU4ufdLkTrkhc0".encodeToByteArray())
    d.toHexString(HexFormat.Default).also { println(it) } // 37b7c679596e2d220a5a05d6ab8a7336e02f1817
}
