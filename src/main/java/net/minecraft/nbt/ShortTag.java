package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag extends NumericTag {
    public static final TagType<ShortTag> TYPE = new TagType<>() {
        public ShortTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(80L);
            return ShortTag.valueOf(local1_1.readShort());
        }

        public String getName() {
            return "SHORT";
        }

        public String getPrettyName() {
            return "TAG_Short";
        }

        public boolean isValue() {
            return true;
        }
    };
    private final short data;

    private ShortTag(short local0_1) {
        this.data = local0_1;
    }

    public static ShortTag valueOf(short local1_0) {
        return local1_0 >= -128 && local1_0 <= 1024 ? ShortTag.Cache.cache[local1_0 + 128] : new ShortTag(local1_0);
    }

    public void write(DataOutput local2_1) throws IOException {
        local2_1.writeShort(this.data);
    }

    public byte getId() {
        return 2;
    }

    public TagType<ShortTag> getType() {
        return TYPE;
    }

    public String toString() {
        return this.data + "s";
    }

    public ShortTag copy() {
        return this;
    }

    public boolean equals(Object local7_1) {
        if (this == local7_1) {
            return true;
        } else {
            return local7_1 instanceof ShortTag && this.data == ((ShortTag) local7_1).data;
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
        return (byte) (this.data & 255);
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
        static final ShortTag[] cache = new ShortTag[1153];

        static {
            for (int local0_0 = 0; local0_0 < cache.length; ++local0_0) {
                cache[local0_0] = new ShortTag((short) (-128 + local0_0));
            }

        }
    }
}
