package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.bytes.ByteSet;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ListTag extends CollectionTag<Tag> {
    public static final TagType<ListTag> TYPE = new TagType<>() {
        public ListTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(296L);
            if (local1_2 > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            } else {
                byte local1_4 = local1_1.readByte();
                int local1_5 = local1_1.readInt();
                if (local1_4 == 0 && local1_5 > 0) {
                    throw new RuntimeException("Missing type on ListTag");
                } else {
                    local1_3.accountBits(32L * (long) local1_5);
                    TagType<?> local1_6 = TagTypes.getType(local1_4);
                    List<Tag> local1_7 = Lists.newArrayListWithCapacity(local1_5);

                    for (int local1_8 = 0; local1_8 < local1_5; ++local1_8) {
                        local1_7.add(local1_6.load(local1_1, local1_2 + 1, local1_3));
                    }

                    return new ListTag(local1_7, local1_4);
                }
            }
        }

        public String getName() {
            return "LIST";
        }

        public String getPrettyName() {
            return "TAG_List";
        }
    };
    private static final ByteSet INLINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList((byte) 1, (byte) 2,
            (byte) 3, (byte) 4, (byte) 5, (byte) 6));
    private final List<Tag> list;
    private byte type;

    private ListTag(List<Tag> local0_1, byte local0_2) {
        this.list = local0_1;
        this.type = local0_2;
    }

    public ListTag() {
        this(Lists.newArrayList(), (byte) 0);
    }

    public void write(DataOutput local2_1) throws IOException {
        if (this.list.isEmpty()) {
            this.type = 0;
        } else {
            this.type = this.list.get(0).getId();
        }

        local2_1.writeByte(this.type);
        local2_1.writeInt(this.list.size());

        for (Tag local2_2 : this.list) {
            local2_2.write(local2_1);
        }

    }

    public byte getId() {
        return 9;
    }

    public TagType<ListTag> getType() {
        return TYPE;
    }

    public String toString() {
        StringBuilder local5_1 = new StringBuilder("[");

        for (int local5_2 = 0; local5_2 < this.list.size(); ++local5_2) {
            if (local5_2 != 0) {
                local5_1.append(',');
            }

            local5_1.append(this.list.get(local5_2));
        }

        return local5_1.append(']').toString();
    }

    private void updateTypeAfterRemove() {
        if (this.list.isEmpty()) {
            this.type = 0;
        }

    }

    public Tag remove(int local7_1) {
        Tag local7_2 = this.list.remove(local7_1);
        this.updateTypeAfterRemove();
        return local7_2;
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public CompoundTag getCompound(int local9_1) {
        if (local9_1 >= 0 && local9_1 < this.list.size()) {
            Tag local9_2 = this.list.get(local9_1);
            if (local9_2.getId() == 10) {
                return (CompoundTag) local9_2;
            }
        }

        return new CompoundTag();
    }

    public ListTag getList(int local10_1) {
        if (local10_1 >= 0 && local10_1 < this.list.size()) {
            Tag local10_2 = this.list.get(local10_1);
            if (local10_2.getId() == 9) {
                return (ListTag) local10_2;
            }
        }

        return new ListTag();
    }

    public short getShort(int local11_1) {
        if (local11_1 >= 0 && local11_1 < this.list.size()) {
            Tag local11_2 = this.list.get(local11_1);
            if (local11_2.getId() == 2) {
                return ((ShortTag) local11_2).getAsShort();
            }
        }

        return 0;
    }

    public int getInt(int local12_1) {
        if (local12_1 >= 0 && local12_1 < this.list.size()) {
            Tag local12_2 = this.list.get(local12_1);
            if (local12_2.getId() == 3) {
                return ((IntTag) local12_2).getAsInt();
            }
        }

        return 0;
    }

    public int[] getIntArray(int local13_1) {
        if (local13_1 >= 0 && local13_1 < this.list.size()) {
            Tag local13_2 = this.list.get(local13_1);
            if (local13_2.getId() == 11) {
                return ((IntArrayTag) local13_2).getAsIntArray();
            }
        }

        return new int[0];
    }

    public double getDouble(int local14_1) {
        if (local14_1 >= 0 && local14_1 < this.list.size()) {
            Tag local14_2 = this.list.get(local14_1);
            if (local14_2.getId() == 6) {
                return ((DoubleTag) local14_2).getAsDouble();
            }
        }

        return 0.0D;
    }

    public float getFloat(int local15_1) {
        if (local15_1 >= 0 && local15_1 < this.list.size()) {
            Tag local15_2 = this.list.get(local15_1);
            if (local15_2.getId() == 5) {
                return ((FloatTag) local15_2).getAsFloat();
            }
        }

        return 0.0F;
    }

    public String getString(int local16_1) {
        if (local16_1 >= 0 && local16_1 < this.list.size()) {
            Tag local16_2 = this.list.get(local16_1);
            return local16_2.getId() == 8 ? local16_2.getAsString() : local16_2.toString();
        } else {
            return "";
        }
    }

    public int size() {
        return this.list.size();
    }

    public Tag get(int local18_1) {
        return this.list.get(local18_1);
    }

    public Tag set(int local19_1, Tag local19_2) {
        Tag local19_3 = this.get(local19_1);
        if (!this.setTag(local19_1, local19_2)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d",
                    local19_2.getId(), this.type));
        } else {
            return local19_3;
        }
    }

    public void add(int local20_1, Tag local20_2) {
        if (!this.addTag(local20_1, local20_2)) {
            throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d",
                    local20_2.getId(), this.type));
        }
    }

    public boolean setTag(int local21_1, Tag local21_2) {
        if (this.updateType(local21_2)) {
            this.list.set(local21_1, local21_2);
            return true;
        } else {
            return false;
        }
    }

    public boolean addTag(int local22_1, Tag local22_2) {
        if (this.updateType(local22_2)) {
            this.list.add(local22_1, local22_2);
            return true;
        } else {
            return false;
        }
    }

    private boolean updateType(Tag local23_1) {
        if (local23_1.getId() == 0) {
            return false;
        } else if (this.type == 0) {
            this.type = local23_1.getId();
            return true;
        } else {
            return this.type == local23_1.getId();
        }
    }

    public ListTag copy() {
        Iterable<Tag> local24_1 = TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list,
                Tag::copy);
        List<Tag> local24_2 = Lists.newArrayList(local24_1);
        return new ListTag(local24_2, this.type);
    }

    public boolean equals(Object local25_1) {
        if (this == local25_1) {
            return true;
        } else {
            return local25_1 instanceof ListTag && Objects.equals(this.list, ((ListTag) local25_1).list);
        }
    }

    public int hashCode() {
        return this.list.hashCode();
    }

    public int getElementType() {
        return this.type;
    }

    public void clear() {
        this.list.clear();
        this.type = 0;
    }
}
