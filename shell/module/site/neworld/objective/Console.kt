package site.neworld.objective

object Console {
    fun readLn() = readLine()!!
    fun readLnByte() = readLn().toByte()
    fun readLnShort() = readLn().toShort()
    fun readLnInt() = readLn().toInt()
    fun readLnLong() = readLn().toLong()
    fun readLnFloat() = readLn().toFloat()
    fun readLnDouble() = readLn().toDouble()
    fun readLnBigInt(radix: Int = 10) = readLn().toBigInteger(radix)
    fun readLnBigDecimal() = readLn().toBigDecimal()

    fun lineSequence(limit: Int = Int.MAX_VALUE) = generateSequence { readLine() }.constrainOnce().take(limit)
    fun readLnStrings() = readLn().split(' ')
    fun readLnBytes() = readLnStrings().map { it.toByte() }
    fun readLnShorts() = readLnStrings().map { it.toShort() }
    fun readLnInts() = readLnStrings().map { it.toInt() }
    fun readLnLongs() = readLnStrings().map { it.toLong() }
    fun readLnFloats() = readLnStrings().map { it.toFloat() }
    fun readLnDoubles() = readLnStrings().map { it.toDouble() }

    fun readByteArray() = readLnStrings().run { ByteArray(size) { get(it).toByte() } }
    fun readShortArray() = readLnStrings().run { ShortArray(size) { get(it).toShort() } }
    fun readIntArray() = readLnStrings().run { IntArray(size) { get(it).toInt() } }
    fun readLongArray() = readLnStrings().run { LongArray(size) { get(it).toLong() } }
    fun readFloatArray() = readLnStrings().run { FloatArray(size) { get(it).toFloat() } }
    fun readDoubleArray() = readLnStrings().run { DoubleArray(size) { get(it).toDouble() } }

    fun readLnByteArray(n: Int) = ByteArray(n) { readLnByte() }
    fun readLnShortArray(n: Int) = ShortArray(n) { readLnShort() }
    fun readLnIntArray(n: Int) = IntArray(n) { readLnInt() }
    fun readLnLongArray(n: Int) = LongArray(n) { readLnLong() }
    fun readLnFloatArray(n: Int) = FloatArray(n) { readLnFloat() }
    fun readLnDoubleArray(n: Int) = DoubleArray(n) { readLnDouble() }

    fun readByteArray2d(rows: Int, cols: Int) = Array(rows) { readByteArray().also { require(it.size == cols) } }
    fun readShortArray2d(rows: Int, cols: Int) = Array(rows) { readShortArray().also { require(it.size == cols) } }
    fun readLongArray2d(rows: Int, cols: Int) = Array(rows) { readLongArray().also { require(it.size == cols) } }
    fun readIntArray2d(rows: Int, cols: Int) = Array(rows) { readIntArray().also { require(it.size == cols) } }
    fun readFloatArray2d(rows: Int, cols: Int) = Array(rows) { readFloatArray().also { require(it.size == cols) } }
    fun readDoubleArray2d(rows: Int, cols: Int) = Array(rows) { readDoubleArray().also { require(it.size == cols) } }

    fun isWhiteSpace(c: Char) = c in " \r\n\t"

    // JVM-only targeting code follows next
    // readString() via sequence is still slightly faster than Scanner
    fun readString() = generateSequence { System.`in`.read().toChar() }
            .dropWhile { isWhiteSpace(it) }.takeWhile { !isWhiteSpace(it) }.joinToString("")
    fun readByte() = readString().toByte()
    fun readShort() = readString().toShort()
    fun readInt() = readString().toInt()
    fun readLong() = readString().toLong()
    fun readFloat() = readString().toFloat()
    fun readDouble() = readString().toDouble()
    fun readBigInt(radix: Int = 10) = readString().toBigInteger(radix)
    fun readBigDecimal() = readString().toBigDecimal()

    fun readBytes(n: Int) = generateSequence { readByte() }.take(n)
    fun readShorts(n: Int) = generateSequence { readShort() }.take(n)
    fun readInts(n: Int) = generateSequence { readInt() }.take(n)
    fun readLongs(n: Int) = generateSequence { readLong() }.take(n)
    fun readFloats(n: Int) = generateSequence { readFloat() }.take(n)
    fun readDoubles(n: Int) = generateSequence { readDouble() }.take(n)
}