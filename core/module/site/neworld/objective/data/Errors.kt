package site.neworld.objective.data

import org.koin.core.KoinComponent
import org.koin.core.inject
import site.neworld.objective.utils.ChunkPos

enum class ErrorCategory {
    // Just some information about activities
    NON_ISSUE,
    // The core operation is experiencing performance degradation
    DEGRADATION,
    // Some data is incorrect but can be handled programatically to recover most of the data
    INCONSISTENCY,
    // A part of the expected data is missing or in unexpected form and the involved entry is lost indefinitely
    // but the server can continue to operate normally (can be configured to stop)
    PARTIAL_CORRUPTION,
    // An important part of the expected data is missing or in unexpected form
    // the server cannot continue to operate and shall be shutdown immediately
    // expecting human intervention
    FATAL_CORRUPTION,
    // The configured storage backend has failed
    // the server cannot continue to operate and shall be shutdown immediately
    // expecting human intervention
    FATAL_BACKEND
}

interface IErrorRecorder {
    fun accept(binId: Int, category: ErrorCategory, pos: ChunkPos, vararg meta: Number)
}

private object ErrorRecorderGetter: KoinComponent {
    val recorder: IErrorRecorder by inject()
}

enum class Warning(private val binId: Int, private val category: ErrorCategory) {
    // performance
    QUEUE_DEPTH(0x00001, ErrorCategory.DEGRADATION), // I/O queue depth exceeded configured limit
    STORAGE_BACKEND(0x00002, ErrorCategory.DEGRADATION), // system detected a storage backend bottleneck
    // data issues
    DUAL_STREAM(0x10001, ErrorCategory.INCONSISTENCY);

    fun issue(pos: ChunkPos, vararg meta: Number) = ErrorRecorderGetter.recorder.accept(binId, category, pos, *meta)
}

class AnvilDbException(val category: Error, val position:ChunkPos): Throwable()

enum class Error(private val binId: Int, private val category: ErrorCategory) {
    INVALID_STREAM_SIZE(0x20001, ErrorCategory.PARTIAL_CORRUPTION),
    TRUNCATED_STREAM_HEADER(0x20002, ErrorCategory.PARTIAL_CORRUPTION),
    TRUNCATED_STREAM(0x20003, ErrorCategory.PARTIAL_CORRUPTION),
    VOID_STREAM(0x20004, ErrorCategory.PARTIAL_CORRUPTION),
    BULK_HEADER_TRUNCATED(0x30001, ErrorCategory.FATAL_CORRUPTION),
    BULK_TRUNCATED_FILE(0x30002, ErrorCategory.FATAL_CORRUPTION), // triggered when 0 bytes is read from the file
    // backend
    MALFORMED_INDEX(0x40001, ErrorCategory.FATAL_BACKEND),
    STORAGE_SPACE(0x40002, ErrorCategory.FATAL_BACKEND),
    PERMISSION(0x40003, ErrorCategory.FATAL_BACKEND);

    fun raise(pos: ChunkPos, vararg meta: Number): Nothing {
        ErrorRecorderGetter.recorder.accept(binId, category, pos, *meta)
        throw AnvilDbException(this, pos)
    }
}
