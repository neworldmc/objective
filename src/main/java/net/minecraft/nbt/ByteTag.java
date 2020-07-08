package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag extends NumericTag {
    public static final TagType<ByteTag> TYPE = new TagType<>() {
        public ByteTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(72L);
            return ByteTag.valueOf(local1_1.readByte());
        }

        public String getName() {
            return "BYTE";
        }

        public String getPrettyName() {
            return "TAG_Byte";
        }

        public boolean isValue() {
            return true;
        }
    };
    public static final ByteTag ZERO = valueOf((byte) 0);
    public static final ByteTag ONE = valueOf((byte) 1);
    private final byte data;

    private ByteTag(byte local0_1) {
        this.data = local0_1;
    }

    public static ByteTag valueOf(byte local1_0) {
        return ByteTag.Cache.cache[128 + local1_0];
    }

    public static ByteTag valueOf(boolean local2_0) {
        return local2_0 ? ONE : ZERO;
    }

    public void write(DataOutput local3_1) throws IOException {
        local3_1.writeByte(this.data);
    }

    public byte getId() {
        return 1;
    }

    public TagType<ByteTag> getType() {
        return TYPE;
    }

    public String toString() {
        return this.data + "b";
    }

    public ByteTag copy() {
        return this;
    }

    public boolean equals(Object local8_1) {
        if (this == local8_1) {
            return true;
        } else {
            return local8_1 instanceof ByteTag && this.data == ((ByteTag) local8_1).data;
        }
    }

    public int hashCode() {
        return this.data;
    }

    public long getAsLong() {
        return this.data;
    }

    public int getAsInt() {
        return this.data;
    }

    public short getAsShort() {
        return this.data;
    }

    public byte getAsByte() {
        return this.data;
    }

    public double getAsDouble() {
        return this.data;
    }

    public float getAsFloat() {
        return this.data;
    }

    public Number getAsNumber() {
        return this.data;
    }

    static class Cache {
        private static final ByteTag[] cache = new ByteTag[256];

        static {
            for (int local1_0 = 0; local1_0 < cache.length; ++local1_0) {
                cache[local1_0] = new ByteTag((byte) (local1_0 - 128));
            }

        }
    }
}
