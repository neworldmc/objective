package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatTag extends NumericTag {
    public static final FloatTag ZERO = new FloatTag(0.0F);
    public static final TagType<FloatTag> TYPE = new TagType<>() {
        public FloatTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(96L);
            return FloatTag.valueOf(local1_1.readFloat());
        }

        public String getName() {
            return "FLOAT";
        }

        public String getPrettyName() {
            return "TAG_Float";
        }

        public boolean isValue() {
            return true;
        }
    };
    private final float data;

    private FloatTag(float local0_1) {
        this.data = local0_1;
    }

    public static FloatTag valueOf(float local1_0) {
        return local1_0 == 0.0F ? ZERO : new FloatTag(local1_0);
    }

    public void write(DataOutput local2_1) throws IOException {
        local2_1.writeFloat(this.data);
    }

    public byte getId() {
        return 5;
    }

    public TagType<FloatTag> getType() {
        return TYPE;
    }

    public String toString() {
        return this.data + "f";
    }

    public FloatTag copy() {
        return this;
    }

    public boolean equals(Object local7_1) {
        if (this == local7_1) {
            return true;
        } else {
            return local7_1 instanceof FloatTag && this.data == ((FloatTag) local7_1).data;
        }
    }

    public int hashCode() {
        return Float.floatToIntBits(this.data);
    }

    public long getAsLong() {
        return (long) this.data;
    }

    public int getAsInt() {
        return (int) Math.floor(this.data);
    }

    public short getAsShort() {
        return (short) ((int) Math.floor(this.data) & '\uffff');
    }

    public byte getAsByte() {
        return (byte) ((int) Math.floor(this.data) & 255);
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
}
