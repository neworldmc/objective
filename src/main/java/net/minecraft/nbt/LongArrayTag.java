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
        @Override
        public boolean isValue() {
            return false;
        }

        public LongArrayTag load(DataInput input, int depth, SizeFence fence) throws IOException {
            fence.accountBits(192L);
            int local1_4 = input.readInt();
            fence.accountBits(64L * (long) local1_4);
            long[] local1_5 = new long[local1_4];

            for (int local1_6 = 0; local1_6 < local1_4; ++local1_6) {
                local1_5[local1_6] = input.readLong();
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

        for (int i = 0; i < local3_0.size(); ++i) {
            Long v = local3_0.get(i);
            local3_1[i] = v == null ? 0L : v;
        }

        return local3_1;
    }

    public void write(DataOutput output) throws IOException {
        output.writeInt(this.data.length);
        for (long local4_2 : this.data) output.writeLong(local4_2);
    }

    public byte getId() {
        return 12;
    }

    public TagType<LongArrayTag> getType() {
        return TYPE;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[L;");
        for (int i = 0; i < this.data.length; ++i) {
            if (i != 0) builder.append(',');
            builder.append(this.data[i]).append('L');
        }
        return builder.append(']').toString();
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
