package site.neworld.objective.data

import java.io.InputStream
import java.io.OutputStream

enum class Compression(val id: Int,
                                private val inputWrapper: (InputStream) -> InputStream,
                                private val outputWrapper: (OutputStream) -> OutputStream) {
    GZIP(1, { java.util.zip.GZIPInputStream(it) }, { java.util.zip.GZIPOutputStream(it) }),
    DEFLATE(2, { java.util.zip.InflaterInputStream(it) }, { java.util.zip.DeflaterOutputStream(it) }),
    NONE(3, { it }, { it });

    companion object {
        private val VERSIONS = values().associateBy { it.id }
        fun fromId(id: Int) = VERSIONS[id] ?: throw IllegalArgumentException("invalid version id")
    }

    fun wrap(inputStream: InputStream) = inputWrapper(inputStream).buffered()
    fun wrap(outputStream: OutputStream) = outputWrapper(outputStream).buffered()
}