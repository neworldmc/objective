package net.minecraft.nbt;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LongArrayTag extends CollectionTag<LongTag> {
    public static final TagType<LongArrayTag> TYPE = new TagType<>() {
        public LongArrayTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(192L);
            int local1_4 = local1_1.readInt();
            local1_3.accountBits(64L * (long) local1_4);
            long[] local1_5 = new long[local1_4];

            for (int local1_6 = 0; local1_6 < local1_4; ++local1_6) {
                local1_5[local1_6] = local1_1.readLong();
            }

            return new LongArrayTag(local1_5);
        }

        public String getName() {
            return "LONG[]";
        }

        public String getPrettyName() {
            return "TAG_Long_Array";
        }
    };
    private long[] data;

    public LongArrayTag(long[] local0_1) {
        this.data = local0_1;
    }

    public LongArrayTag(LongSet local1_1) {
        this.data = local1_1.toLongArray();
    }

    public LongArrayTag(List<Long> local2_1) {
        this(toArray(local2_1));
    }

    private static long[] toArray(List<Long> local3_0) {
        long[] local3_1 = new long[local3_0.size()];

        for (int local3_2 = 0; local3_2 < local3_0.size(); ++local3_2) {
            Long local3_3 = local3_0.get(local3_2);
            local3_1[local3_2] = local3_3 == null ? 0L : local3_3;
        }

        return local3_1;
    }

    public void write(DataOutput local4_1) throws IOException {
        local4_1.writeInt(this.data.length);
        long[] var2 = this.data;
        int var3 = var2.length;

        for (long local4_2 : var2) {
            local4_1.writeLong(local4_2);
        }

    }

    public byte getId() {
        return 12;
    }

    public TagType<LongArrayTag> getType() {
        return TYPE;
    }

    public String toString() {
        StringBuilder local7_1 = new StringBuilder("[L;");

        for (int local7_2 = 0; local7_2 < this.data.length; ++local7_2) {
            if (local7_2 != 0) {
                local7_1.append(',');
            }

            local7_1.append(this.data[local7_2]).append('L');
        }

        return local7_1.append(']').toString();
    }

    public LongArrayTag copy() {
        long[] local8_1 = new long[this.data.length];
        System.arraycopy(this.data, 0, local8_1, 0, this.data.length);
        return new LongArrayTag(local8_1);
    }

    public boolean equals(Object local9_1) {
        if (this == local9_1) {
            return true;
        } else {
            return local9_1 instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag) local9_1).data);
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    public long[] getAsLongArray() {
        return this.data;
    }

    public int size() {
        return this.data.length;
    }

    public LongTag get(int local14_1) {
        return LongTag.valueOf(this.data[local14_1]);
    }

    public LongTag set(int local15_1, LongTag local15_2) {
        long local15_3 = this.data[local15_1];
        this.data[local15_1] = local15_2.getAsLong();
        return LongTag.valueOf(local15_3);
    }

    public void add(int local16_1, LongTag local16_2) {
        this.data = ArrayUtils.insert(local16_1, this.data, local16_2.getAsLong());
    }

    public boolean setTag(int local17_1, Tag local17_2) {
        if (local17_2 instanceof NumericTag) {
            this.data[local17_1] = ((NumericTag) local17_2).getAsLong();
            return true;
        } else {
            return false;
        }
    }

    public boolean addTag(int local18_1, Tag local18_2) {
        if (local18_2 instanceof NumericTag) {
            this.data = ArrayUtils.insert(local18_1, this.data, ((NumericTag) local18_2).getAsLong());
            return true;
        } else {
            return false;
        }
    }

    public LongTag remove(int local19_1) {
        long local19_2 = this.data[local19_1];
        this.data = ArrayUtils.remove(this.data, local19_1);
        return LongTag.valueOf(local19_2);
    }

    public void clear() {
        this.data = new long[0];
    }
}
