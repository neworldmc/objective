package site.neworld.objective.io

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.*
import net.minecraft.core.ChunkPos
import net.minecraft.nbt.CompoundTag
import java.util.concurrent.Executors

// TODO: implement transaction control and fine-grained concurrency protection
// As file-storage media is usually on a slower tier, and various processing stages have different characteristics
// To ensure consistent response time, high throughput and performance, this component shall be implemented with
// coroutine

// Also, though it is not the intention to implement a cache at this level, as I/O operations can take a long time,
// which might result in a overlap of earlier write operations and a read operation afterwards,
// the chunk data in it's I/O format shall be preserved during any write operations, and shall be delivered
// immediately on any read request of the same chunk without reaching the actual storage.

// Intended behaviour for overlapped requests (current-new):
// READ-READ: Only one read operation is performed and returned on both path
// READ-WRITE: The former read operation from the storage shall be effectively cancelled, the data to write shall
//     be returned on the read request immediately
// WRITE-READ: The data to write shall be returned immediately to the read request
// WRITE-WRITE: The former write operation shall be cancelled effectively, any effect shall be effectively undone
//     and only the later shall be actually performed.

// Intended execution model: 1 sync response worker + N compute worker, I/O in full async operation


class LevelContext(private val level: Region) {
    private val asyncComputeContext = Executors.newWorkStealingPool().asCoroutineDispatcher()

    fun requestRead(data: ByteBuf) = runBlocking {
        launch(asyncComputeContext) {
            level.read(parseReadPacket(data))
        }
    }

    private fun parseReadPacket(data: ByteBuf): ChunkPos {
        TODO("Not yet implemented")
    }

    fun requestWrite(data: ByteBuf) = runBlocking {
        launch(asyncComputeContext) {
            //level.write(parseWritePacket(data))
        }
    }
}