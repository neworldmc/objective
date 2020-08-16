package site.neworld.objective

import org.koin.core.context.startKoin
import site.neworld.objective.data.Concurrency
import site.neworld.objective.data.consoleRecorder

fun main() {
    startKoin {
        modules(consoleRecorder)
    }
    Concurrency.join()
}
