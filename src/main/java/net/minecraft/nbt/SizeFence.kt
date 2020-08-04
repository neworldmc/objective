package net.minecraft.nbt

open class SizeFence(private val quota: Long) {
    private var usage: Long = 0

    open fun accountBits(bits: Long) {
        usage += bits / 8L
        if (usage > quota) {
            throw RuntimeException("Tried to read NBT tag that was too big; tried to allocate: ${usage}bytes where max allowed: $quota")
        }
    }

    companion object {
        val UNLIMITED = object : SizeFence(0L) {
            override fun accountBits(bits: Long) {}
        }
    }
}