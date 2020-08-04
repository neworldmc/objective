package net.minecraft.nbt;

import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IntArrayTag extends CollectionTag<IntTag> {
    public static final TagType<IntArrayTag> TYPE = new TagType<>() {
        @Override
        public boolean isValue() {
            return false;
        }

        public IntArrayTag load(DataInput input, int depth, SizeFence fence) throws IOException {
            fence.accountBits(192L);
            int local1_4 = input.readInt();
            fence.accountBits(32L * (long) local1_4);
            int[] local1_5 = new int[local1_4];

            for (int local1_6 = 0; local1_6 < local1_4; ++local1_6) {
                local1_5[local1_6] = input.readInt();
            }

            return new IntArrayTag(local1_5);
        }

        public String getName() {
            return "INT[]";
        }

        public String getPrettyName() {
            return "TAG_Int_Array";
        }
    };
    private int[] data;

    public IntArrayTag(int[] local0_1) {
        this.data = local0_1;
    }

    public IntArrayTag(List<Integer> local1_1) {
        this(toArray(local1_1));
    }

    private static int[] toArray(List<Integer> local2_0) {
        int[] local2_1 = new int[local2_0.size()];

        for (int local2_2 = 0; local2_2 < local2_0.size(); ++local2_2) {
            Integer local2_3 = local2_0.get(local2_2);
            local2_1[local2_2] = local2_3 == null ? 0 : local2_3;
        }

        return local2_1;
    }

    public void write(DataOutput output) throws IOException {
        output.writeInt(this.data.length);
        int[] var2 = this.data;

        for (int local3_2 : var2) {
            output.writeInt(local3_2);
        }
    }

    public byte getId() {
        return 11;
    }

    public TagType<IntArrayTag> getType() {
        return TYPE;
    }

    public String toString() {
        StringBuilder local6_1 = new StringBuilder("[I;");

        for (int local6_2 = 0; local6_2 < this.data.length; ++local6_2) {
            if (local6_2 != 0) {
                local6_1.append(',');
            }

            local6_1.append(this.data[local6_2]);
        }

        return local6_1.append(']').toString();
    }

    public IntArrayTag copy() {
        int[] local7_1 = new int[this.data.length];
        System.arraycopy(this.data, 0, local7_1, 0, this.data.length);
        return new IntArrayTag(local7_1);
    }

    public boolean equals(Object local8_1) {
        if (this == local8_1) {
            return true;
        } else {
            return local8_1 instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag) local8_1).data);
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    public int[] getAsIntArray() {
        return this.data;
    }

    public int size() {
        return this.data.length;
    }

    public IntTag get(int local13_1) {
        return IntTag.valueOf(this.data[local13_1]);
    }

    public IntTag set(int local14_1, IntTag local14_2) {
        int local14_3 = this.data[local14_1];
        this.data[local14_1] = local14_2.getAsInt();
        return IntTag.valueOf(local14_3);
    }

    public void add(int local15_1, IntTag local15_2) {
        this.data = ArrayUtils.insert(local15_1, this.data, local15_2.getAsInt());
    }

    public boolean setTag(int local16_1, Tag local16_2) {
        if (local16_2 instanceof NumericTag) {
            this.data[local16_1] = ((NumericTag) local16_2).getAsInt();
            return true;
        } else {
            return false;
        }
    }

    public boolean addTag(int local17_1, Tag local17_2) {
        if (local17_2 instanceof NumericTag) {
            this.data = ArrayUtils.insert(local17_1, this.data, ((NumericTag) local17_2).getAsInt());
            return true;
        } else {
            return false;
        }
    }

    public IntTag remove(int local18_1) {
        int local18_2 = this.data[local18_1];
        this.data = ArrayUtils.remove(this.data, local18_1);
        return IntTag.valueOf(local18_2);
    }

    public void clear() {
        this.data = new int[0];
    }
}
