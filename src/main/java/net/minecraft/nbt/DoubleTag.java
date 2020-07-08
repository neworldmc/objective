package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleTag extends NumericTag {
    public static final DoubleTag ZERO = new DoubleTag(0.0D);
    public static final TagType<DoubleTag> TYPE = new TagType<>() {
        public DoubleTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(128L);
            return DoubleTag.valueOf(local1_1.readDouble());
        }

        public String getName() {
            return "DOUBLE";
        }

        public String getPrettyName() {
            return "TAG_Double";
        }

        public boolean isValue() {
            return true;
        }
    };
    private final double data;

    private DoubleTag(double local0_1) {
        this.data = local0_1;
    }

    public static DoubleTag valueOf(double local1_0) {
        return local1_0 == 0.0D ? ZERO : new DoubleTag(local1_0);
    }

    public void write(DataOutput local2_1) throws IOException {
        local2_1.writeDouble(this.data);
    }

    public byte getId() {
        return 6;
    }

    public TagType<DoubleTag> getType() {
        return TYPE;
    }

    public String toString() {
        return this.data + "d";
    }

    public DoubleTag copy() {
        return this;
    }

    public boolean equals(Object local7_1) {
        if (this == local7_1) {
            return true;
        } else {
            return local7_1 instanceof DoubleTag && this.data == ((DoubleTag) local7_1).data;
        }
    }

    public int hashCode() {
        long local8_1 = Double.doubleToLongBits(this.data);
        return (int) (local8_1 ^ local8_1 >>> 32);
    }

    public long getAsLong() {
        return (long) Math.floor(this.data);
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
        return (float) this.data;
    }

    public Number getAsNumber() {
        return this.data;
    }
}
