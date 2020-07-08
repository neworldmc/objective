package net.minecraft.nbt;

import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ByteArrayTag extends CollectionTag<ByteTag> {
    public static final TagType<ByteArrayTag> TYPE = new TagType<>() {
        public ByteArrayTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(192L);
            int local1_4 = local1_1.readInt();
            local1_3.accountBits(8L * (long) local1_4);
            byte[] local1_5 = new byte[local1_4];
            local1_1.readFully(local1_5);
            return new ByteArrayTag(local1_5);
        }

        public String getName() {
            return "BYTE[]";
        }

        public String getPrettyName() {
            return "TAG_Byte_Array";
        }
    };
    private byte[] data;

    public ByteArrayTag(byte[] local0_1) {
        this.data = local0_1;
    }

    public ByteArrayTag(List<Byte> local1_1) {
        this(toArray(local1_1));
    }

    private static byte[] toArray(List<Byte> local2_0) {
        byte[] local2_1 = new byte[local2_0.size()];

        for (int local2_2 = 0; local2_2 < local2_0.size(); ++local2_2) {
            Byte local2_3 = local2_0.get(local2_2);
            local2_1[local2_2] = local2_3 == null ? 0 : local2_3;
        }

        return local2_1;
    }

    public void write(DataOutput local3_1) throws IOException {
        local3_1.writeInt(this.data.length);
        local3_1.write(this.data);
    }

    public byte getId() {
        return 7;
    }

    public TagType<ByteArrayTag> getType() {
        return TYPE;
    }

    public String toString() {
        StringBuilder local6_1 = new StringBuilder("[B;");

        for (int local6_2 = 0; local6_2 < this.data.length; ++local6_2) {
            if (local6_2 != 0) {
                local6_1.append(',');
            }

            local6_1.append(this.data[local6_2]).append('B');
        }

        return local6_1.append(']').toString();
    }

    public Tag copy() {
        byte[] local7_1 = new byte[this.data.length];
        System.arraycopy(this.data, 0, local7_1, 0, this.data.length);
        return new ByteArrayTag(local7_1);
    }

    public boolean equals(Object local8_1) {
        if (this == local8_1) {
            return true;
        } else {
            return local8_1 instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag) local8_1).data);
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    public byte[] getAsByteArray() {
        return this.data;
    }

    public int size() {
        return this.data.length;
    }

    public ByteTag get(int local13_1) {
        return ByteTag.valueOf(this.data[local13_1]);
    }

    public ByteTag set(int local14_1, ByteTag local14_2) {
        byte local14_3 = this.data[local14_1];
        this.data[local14_1] = local14_2.getAsByte();
        return ByteTag.valueOf(local14_3);
    }

    public void add(int local15_1, ByteTag local15_2) {
        this.data = ArrayUtils.insert(local15_1, this.data, local15_2.getAsByte());
    }

    public boolean setTag(int local16_1, Tag local16_2) {
        if (local16_2 instanceof NumericTag) {
            this.data[local16_1] = ((NumericTag) local16_2).getAsByte();
            return true;
        } else {
            return false;
        }
    }

    public boolean addTag(int local17_1, Tag local17_2) {
        if (local17_2 instanceof NumericTag) {
            this.data = ArrayUtils.insert(local17_1, this.data, ((NumericTag) local17_2).getAsByte());
            return true;
        } else {
            return false;
        }
    }

    public ByteTag remove(int local18_1) {
        byte local18_2 = this.data[local18_1];
        this.data = ArrayUtils.remove(this.data, local18_1);
        return ByteTag.valueOf(local18_2);
    }

    public void clear() {
        this.data = new byte[0];
    }
}
