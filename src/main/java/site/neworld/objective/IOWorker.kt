package site.neworld.objective

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

fun work(requestQueueCapacity: Int): BlockingQueue<WorkerRequest> {
    return LinkedBlockingQueue<WorkerRequest>(requestQueueCapacity).also {
        thread {
            loop@ while (true) {
                val task = it.take()
                when (task.operation) {
                    WorkerOperation.CLOSE -> break@loop
                    WorkerOperation.READ -> TODO()
                    WorkerOperation.WRITE -> TODO()
                }
            }
        }
    }
}

data class WorkerRequest(val operation: WorkerOperation, val callbackReceiver: BlockingQueue<Any>)

enum class WorkerOperation { CLOSE, READ, WRITE }
