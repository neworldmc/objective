package site.neworld.objective.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

private object DbFJWThreadFactory : ForkJoinPool.ForkJoinWorkerThreadFactory {
    private val counter = AtomicInteger(1)

    override fun newThread(pool: ForkJoinPool) = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool).apply {
        name = "[NWMC O-DB FJW]${counter.getAndIncrement()}"
        if (isDaemon) isDaemon = false
        if (priority != Thread.NORM_PRIORITY) priority = Thread.NORM_PRIORITY
    }!!
}

private class SyncDispatcher(private val delegate: CoroutineDispatcher) : CoroutineDispatcher() {
    private val queue = LinkedList<Runnable>()
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (synchronized(queue) {
                    queue.add(block)
                    queue.size == 1
                }) delegate.dispatch(context, Runnable { drain() })
    }

    private tailrec fun drain() {
        synchronized(queue) { queue.first }.run()
        if (synchronized(queue) {
                    queue.removeFirst()
                    queue.isNotEmpty()
                }) drain()
    }
}

object Concurrency {
    private fun newWorkStealingPool() =
            ForkJoinPool(Runtime.getRuntime().availableProcessors(), DbFJWThreadFactory, null, true)

    private val defaultConcurrentExecutor = newWorkStealingPool()

    val defaultCoroutineContext = defaultConcurrentExecutor.asCoroutineDispatcher()

    fun newSynchronizedCoroutineContext(): CoroutineDispatcher = SyncDispatcher(defaultCoroutineContext)
}