package site.neworld.objective

import org.koin.core.context.startKoin
import site.neworld.objective.data.consoleRecorder
import site.neworld.objective.network.runServer
import java.net.InetAddress

fun main() {
    startKoin {
        modules(consoleRecorder)
    }
    runServer(InetAddress.getByName("127.0.0.1"), 23333)
}
