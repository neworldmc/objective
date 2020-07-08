package net.minecraft.core;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

public class ChunkPos {
    public static final long INVALID_CHUNK_POS = asLong(1875016, 1875016);
    public final int x;
    public final int z;

    public ChunkPos(int local0_1, int local0_2) {
        this.x = local0_1;
        this.z = local0_2;
    }

    public long toLong() {
        return asLong(this.x, this.z);
    }

    public static long asLong(int local4_0, int local4_1) {
        return (long) local4_0 & 4294967295L | ((long) local4_1 & 4294967295L) << 32;
    }

    public int hashCode() {
        int local7_1 = 1664525 * this.x + 1013904223;
        int local7_2 = 1664525 * (this.z ^ -559038737) + 1013904223;
        return local7_1 ^ local7_2;
    }

    public boolean equals(Object local8_1) {
        if (this == local8_1) {
            return true;
        } else if (!(local8_1 instanceof ChunkPos)) {
            return false;
        } else {
            ChunkPos local8_2 = (ChunkPos) local8_1;
            return this.x == local8_2.x && this.z == local8_2.z;
        }
    }

    public int getRegionX() {
        return this.x >> 5;
    }

    public int getRegionZ() {
        return this.z >> 5;
    }

    public int getRegionLocalX() {
        return this.x & 31;
    }

    public int getRegionLocalZ() {
        return this.z & 31;
    }

    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }

}
