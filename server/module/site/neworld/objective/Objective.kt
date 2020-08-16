package site.neworld.objective

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import site.neworld.objective.data.consoleRecorder

fun main() {
    startKoin {
        modules(consoleRecorder)
    }
    val dispatch = site.neworld.objective.data.Concurrency.newSynchronizedCoroutineContext()
    val scope = CoroutineScope(dispatch)
    for (x in 0 .. 10) runBlocking {
        var int = 0
        val end = CompletableDeferred<Unit>()
        val start = System.currentTimeMillis()
        for (i in 0..10000000) {
            scope.launch { if (++int == 10000000) end.complete(Unit) }
        }
        end.await()
        println("$int, ${System.currentTimeMillis() - start}")
    }
    site.neworld.objective.data.Concurrency.join()
    //runServer(InetAddress.getByName("127.0.0.1"), 23333)
}
