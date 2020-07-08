package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends NumericTag {
    public static final TagType<IntTag> TYPE = new TagType<>() {
        public IntTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(96L);
            return IntTag.valueOf(local1_1.readInt());
        }

        public String getName() {
            return "INT";
        }

        public String getPrettyName() {
            return "TAG_Int";
        }

        public boolean isValue() {
            return true;
        }
    };
    private final int data;

    private IntTag(int local0_1) {
        this.data = local0_1;
    }

    public static IntTag valueOf(int local1_0) {
        return local1_0 >= -128 && local1_0 <= 1024 ? IntTag.Cache.cache[local1_0 + 128] : new IntTag(local1_0);
    }

    public void write(DataOutput local2_1) throws IOException {
        local2_1.writeInt(this.data);
    }

    public byte getId() {
        return 3;
    }

    public TagType<IntTag> getType() {
        return TYPE;
    }

    public String toString() {
        return String.valueOf(this.data);
    }

    public IntTag copy() {
        return this;
    }

    public boolean equals(Object local7_1) {
        if (this == local7_1) {
            return true;
        } else {
            return local7_1 instanceof IntTag && this.data == ((IntTag) local7_1).data;
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
        return (short) (this.data & '\uffff');
    }

    public byte getAsByte() {
        return (byte) (this.data & 255);
    }

    public double getAsDouble() {
        return this.data;
    }

    public float getAsFloat() {
        return (float) this.data;
    }

    public Number getAsNumber() {
        return this.data;
    }

    static class Cache {
        static final IntTag[] cache = new IntTag[1153];

        static {
            for (int local0_0 = 0; local0_0 < cache.length; ++local0_0) {
                cache[local0_0] = new IntTag(-128 + local0_0);
            }

        }
    }
}
