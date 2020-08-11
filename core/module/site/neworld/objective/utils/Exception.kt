package site.neworld.objective.utils

class ExceptionAggregator {
    val exceptions = mutableListOf<Throwable>()

    inline fun <R> runCaptured(block: () -> R): R? {
        return try {
            block()
        } catch (e: Throwable) {
            exceptions.add(e); null
        }
    }

    fun complete() {
        if (exceptions.isNotEmpty()) throw exceptions.reduce { first, second -> first.addSuppressed(second); first }
    }

    inline fun <R> run(block: ExceptionAggregator.()->R): R {
        val ret = block()
        complete()
        return ret
    }
}