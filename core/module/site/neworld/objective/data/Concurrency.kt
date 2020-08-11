package site.neworld.objective.data

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

private object DbThreadFactoryCore : ThreadFactory {
    private val counter = AtomicInteger(1)
    private val group = System.getSecurityManager().let { if (it != null) it.threadGroup else Thread.currentThread().threadGroup }

    override fun newThread(r: Runnable) = Thread(group, r, "[NWMC O-DB]${counter.getAndIncrement()}", 0).apply {
        if (isDaemon) isDaemon = false
        if (priority != Thread.NORM_PRIORITY) priority = Thread.NORM_PRIORITY
    }
}

private object DbThreadFactory : ThreadFactory {
    private var executor = ThreadPoolExecutor(0, Int.MAX_VALUE, 60L, TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(), DbThreadFactoryCore)

    override fun newThread(r: Runnable): Thread {
        return CompletableFuture<Thread>().apply {
        executor.execute {
            this.complete(Thread.currentThread())
            r.run()
        }}.get()
    }
}

private object DbFJWThreadFactory : ForkJoinPool.ForkJoinWorkerThreadFactory {
    private val counter = AtomicInteger(1)

    override fun newThread(pool: ForkJoinPool) = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool).apply {
        name = "[NWMC O-DB FJW]${counter.getAndIncrement()}"
        if (isDaemon) isDaemon = false
        if (priority != Thread.NORM_PRIORITY) priority = Thread.NORM_PRIORITY
    }!!
}

@Suppress("MemberVisibilityCanBePrivate")
object Concurrency {
    fun newWorkStealingPool() =
            ForkJoinPool(Runtime.getRuntime().availableProcessors(), DbFJWThreadFactory, null, true)

    fun newSynchronizedExecutor() =
        ThreadPoolExecutor(0, 1, 100L, TimeUnit.MILLISECONDS, LinkedBlockingQueue<Runnable>(), DbThreadFactory)

    fun newSynchronizedCoroutineContext() = newSynchronizedExecutor().asCoroutineDispatcher()

    val defaultConcurrentExecutor = newWorkStealingPool()

    val defaultCoroutineContext = defaultConcurrentExecutor.asCoroutineDispatcher()
}