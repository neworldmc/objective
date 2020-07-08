package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends NumericTag {
    public static final TagType<LongTag> TYPE = new TagType<>() {
        public LongTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(128L);
            return LongTag.valueOf(local1_1.readLong());
        }

        public String getName() {
            return "LONG";
        }

        public String getPrettyName() {
            return "TAG_Long";
        }

        public boolean isValue() {
            return true;
        }
    };
    private final long data;

    private LongTag(long local0_1) {
        this.data = local0_1;
    }

    public static LongTag valueOf(long local1_0) {
        return local1_0 >= -128L && local1_0 <= 1024L ? LongTag.Cache.cache[(int) local1_0 + 128] :
                new LongTag(local1_0);
    }

    public void write(DataOutput local2_1) throws IOException {
        local2_1.writeLong(this.data);
    }

    public byte getId() {
        return 4;
    }

    public TagType<LongTag> getType() {
        return TYPE;
    }

    public String toString() {
        return this.data + "L";
    }

    public LongTag copy() {
        return this;
    }

    public boolean equals(Object local7_1) {
        if (this == local7_1) {
            return true;
        } else {
            return local7_1 instanceof LongTag && this.data == ((LongTag) local7_1).data;
        }
    }

    public int hashCode() {
        return (int) (this.data ^ this.data >>> 32);
    }

    public long getAsLong() {
        return this.data;
    }

    public int getAsInt() {
        return (int) this.data;
    }

    public short getAsShort() {
        return (short) (int) (this.data & 65535L);
    }

    public byte getAsByte() {
        return (byte) (int) (this.data & 255L);
    }

    public double getAsDouble() {
        return (double) this.data;
    }

    public float getAsFloat() {
        return (float) this.data;
    }

    public Number getAsNumber() {
        return this.data;
    }

    static class Cache {
        static final LongTag[] cache = new LongTag[1153];

        static {
            for (int local0_0 = 0; local0_0 < cache.length; ++local0_0) {
                cache[local0_0] = new LongTag(-128 + local0_0);
            }

        }
    }
}
