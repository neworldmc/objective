package site.neworld.objective.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.dsl.module
import site.neworld.objective.utils.ChunkPos
import java.time.LocalDateTime

private class ConsoleErrorRecorder : IErrorRecorder {
    private val exec = Concurrency.newSynchronizedCoroutineContext()
    override fun accept(binId: Int, category: ErrorCategory, pos: ChunkPos, vararg meta: Number) {
        GlobalScope.launch(exec) {
            print("[${LocalDateTime.now()}][$binId][${category.name}]{${pos.toLong()}")
            for (i in meta) print(i)
            println()
        }
    }
}

val consoleRecorder = module {
    single { ConsoleErrorRecorder() as IErrorRecorder }
}