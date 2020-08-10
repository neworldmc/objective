package site.neworld.objective

import site.neworld.objective.network.runServer
import java.net.InetAddress

fun main() {
    runServer(InetAddress.getByName("127.0.0.1"), 23333)
}
