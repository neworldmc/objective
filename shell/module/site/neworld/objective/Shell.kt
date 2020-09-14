package site.neworld.objective

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import site.neworld.objective.data.Concurrency
import site.neworld.objective.data.LevelStorage
import site.neworld.objective.data.consoleRecorder
import site.neworld.objective.utils.ChunkPos
import site.neworld.objective.utils.ExceptionAggregator
import site.neworld.objective.utils.nbt.NbtIo
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File

object Machine {
    private val table = mutableMapOf<String, LevelStorage>()
    private var active: LevelStorage? = null
    private var root: File? = null
    private var activeName: String? = null

    fun boot(dir: File) {
        root = dir
    }

    fun shutdown() = ExceptionAggregator().run {
        for ((_, v) in table) runCaptured { v.close() }
        table.clear()
        active = null
    }

    fun open(name: String) {
        if (!table.containsKey(name)) {
            table[name] = LevelStorage(File(root, name))
            use(name)
        }
    }

    fun close(name: String) {
        if (table.containsKey(name)) {
            table[name]?.close()
            table.remove(name)
        }
    }

    fun use(name: String) {
        if (table.containsKey(name)) {
            active = table[name]
            activeName = name
        }
        else print("No loaded table named $name")
    }

    suspend fun read(x: Int, z: Int) {
        if (active == null) return println("No storage selected")
        active!!.get(ChunkPos(x, z)).let {
            if (it != null) {
                println(NbtIo.read(DataInputStream(ByteArrayInputStream(it))).toString())
            }
            else println("{}")
        }
    }

    suspend fun readSilent(x: Int, z: Int) {
        try {
            if (active == null) return println("No storage selected")
            active!!.get(ChunkPos(x, z))
        } catch(e: Exception) {}
    }

    fun getActive() = activeName
}

tailrec suspend fun runLoop() {
    print("Objective DB[${Machine.getActive().let { it ?: "none" }}] > ")
    val command = Console.readString()
    var exit = false
    when (command) {
        "stop" -> exit = true
        "open" -> Machine.open(Console.readLn())
        "close" -> Machine.close(Console.readLn())
        "use" -> Machine.use(Console.readLn())
        "read" -> Machine.read(Console.readInt(), Console.readLnInt())
        "test" -> {
            for (index in 0..20) {
                val start = System.currentTimeMillis()
                println("test[$index/20] started on $start")
                GlobalScope.launch {
                    for (i in -100..100)
                        for (j in -100..100)
                            GlobalScope.launch {
                                Machine.readSilent(i, j)
                            }
                }.join()
                val end = System.currentTimeMillis()
                val elapsed = end - start
                println("test[$index/20] ended on $end, elapsed $elapsed ms")
            }
        }
        else -> println("unrecognized command: $command ${Console.readLn()}")
    }
    if (!exit) runLoop()
}

tailrec fun getDir(): File {
    println("Please input root dir:")
    val dir = File(Console.readLn())
    return if (dir.isDirectory) dir else {
        println("Given path is not a directory")
        getDir()
    }
}

fun main() {
    startKoin {
        modules(consoleRecorder)
    }
    Machine.boot(getDir())
    runBlocking {
        try {
            runLoop()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    Machine.shutdown()
    Concurrency.join()
}
