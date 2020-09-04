package site.neworld.objective

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import site.neworld.objective.data.Concurrency
import site.neworld.objective.data.LevelStorage
import site.neworld.objective.data.consoleRecorder
import java.io.File

object Controller {
    private var app: KoinApplication? = null
    private var counter = 0

    private fun start() {
        app = startKoin {
            modules(consoleRecorder)
        }
    }

    private fun stop() {
        Concurrency.join()
        app?.close()
        app = null
    }

    fun open(path: File): LevelStorage {
        synchronized(this) { if (counter++ == 0) start() }
        return LevelStorage(path)
    }

    fun close(storage: LevelStorage) {
        storage.close()
        if (synchronized(this) { --counter } == 0) stop()
    }
}
