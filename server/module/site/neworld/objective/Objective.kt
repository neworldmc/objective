package site.neworld.objective

import net.minecraft.core.ChunkPos
import site.neworld.objective.io.Region
import site.neworld.objective.network.runServer
import java.io.File
import java.net.InetAddress

fun main() {
    runServer(InetAddress.getByName("127.0.0.1"), 23333)
}
