package net.minecraft.nbt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class CompoundTag implements Tag {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    public static final TagType<CompoundTag> TYPE = new TagType<>() {
        @Override
        public boolean isValue() {
            return false;
        }

        public CompoundTag load(DataInput input, int depth, SizeFence fence) throws IOException {
            fence.accountBits(384L);
            if (depth > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            } else {
                var local1_4 = Maps.<String, Tag>newHashMap();

                byte local1_5;
                while ((local1_5 = CompoundTag.readNamedTagType(input, fence)) != 0) {
                    String local1_6 = CompoundTag.readNamedTagName(input, fence);
                    fence.accountBits(224 + 16 * local1_6.length());
                    Tag local1_7 = CompoundTag.readNamedTagData(TagTypes.getType(local1_5), local1_6, input,
                            depth + 1, fence);
                    if (local1_4.put(local1_6, local1_7) != null) {
                        fence.accountBits(288L);
                    }
                }

                return new CompoundTag(local1_4);
            }
        }

        public String getName() {
            return "COMPOUND";
        }

        public String getPrettyName() {
            return "TAG_Compound";
        }
    };
    private final Map<String, Tag> tags;

    private CompoundTag(Map<String, Tag> local0_1) {
        this.tags = local0_1;
    }

    public CompoundTag() {
        this(Maps.newHashMap());
    }

    public void write(DataOutput output) throws IOException {

        for (String local2_2 : this.tags.keySet()) {
            Tag local2_3 = this.tags.get(local2_2);
            writeNamedTag(local2_2, local2_3, output);
        }

        output.writeByte(0);
    }

    public Set<String> getAllKeys() {
        return this.tags.keySet();
    }

    public byte getId() {
        return 10;
    }

    public TagType<CompoundTag> getType() {
        return TYPE;
    }

    public int size() {
        return this.tags.size();
    }

    @Nullable
    public Tag put(String local7_1, Tag local7_2) {
        return this.tags.put(local7_1, local7_2);
    }

    public void putByte(String local8_1, byte local8_2) {
        this.tags.put(local8_1, ByteTag.valueOf(local8_2));
    }

    public void putShort(String local9_1, short local9_2) {
        this.tags.put(local9_1, ShortTag.valueOf(local9_2));
    }

    public void putInt(String local10_1, int local10_2) {
        this.tags.put(local10_1, IntTag.valueOf(local10_2));
    }

    public void putLong(String local11_1, long local11_2) {
        this.tags.put(local11_1, LongTag.valueOf(local11_2));
    }

    public void putUUID(String local12_1, UUID local12_2) {
        this.putLong(local12_1 + "Most", local12_2.getMostSignificantBits());
        this.putLong(local12_1 + "Least", local12_2.getLeastSignificantBits());
    }

    public UUID getUUID(String local13_1) {
        return new UUID(this.getLong(local13_1 + "Most"), this.getLong(local13_1 + "Least"));
    }

    public boolean hasUUID(String local14_1) {
        return this.contains(local14_1 + "Most", 99) && this.contains(local14_1 + "Least", 99);
    }

    public void removeUUID(String local15_1) {
        this.remove(local15_1 + "Most");
        this.remove(local15_1 + "Least");
    }

    public void putFloat(String local16_1, float local16_2) {
        this.tags.put(local16_1, FloatTag.valueOf(local16_2));
    }

    public void putDouble(String local17_1, double local17_2) {
        this.tags.put(local17_1, DoubleTag.valueOf(local17_2));
    }

    public void putString(String local18_1, String local18_2) {
        this.tags.put(local18_1, StringTag.valueOf(local18_2));
    }

    public void putByteArray(String local19_1, byte[] local19_2) {
        this.tags.put(local19_1, new ByteArrayTag(local19_2));
    }

    public void putIntArray(String local20_1, int[] local20_2) {
        this.tags.put(local20_1, new IntArrayTag(local20_2));
    }

    public void putIntArray(String local21_1, List<Integer> local21_2) {
        this.tags.put(local21_1, new IntArrayTag(local21_2));
    }

    public void putLongArray(String local22_1, long[] local22_2) {
        this.tags.put(local22_1, new LongArrayTag(local22_2));
    }

    public void putLongArray(String local23_1, List<Long> local23_2) {
        this.tags.put(local23_1, new LongArrayTag(local23_2));
    }

    public void putBoolean(String local24_1, boolean local24_2) {
        this.tags.put(local24_1, ByteTag.valueOf(local24_2));
    }

    @Nullable
    public Tag get(String local25_1) {
        return this.tags.get(local25_1);
    }

    public byte getTagType(String local26_1) {
        Tag local26_2 = this.tags.get(local26_1);
        return local26_2 == null ? 0 : local26_2.getId();
    }

    public boolean contains(String local27_1) {
        return this.tags.containsKey(local27_1);
    }

    public boolean contains(String local28_1, int local28_2) {
        int local28_3 = this.getTagType(local28_1);
        if (local28_3 == local28_2) {
            return true;
        } else if (local28_2 != 99) {
            return false;
        } else {
            return local28_3 == 1 || local28_3 == 2 || local28_3 == 3 || local28_3 == 4 || local28_3 == 5 || local28_3 == 6;
        }
    }

    public byte getByte(String local29_1) {
        try {
            if (this.contains(local29_1, 99)) {
                return ((NumericTag) this.tags.get(local29_1)).getAsByte();
            }
        } catch (ClassCastException ignored) {
        }

        return 0;
    }

    public short getShort(String local30_1) {
        try {
            if (this.contains(local30_1, 99)) {
                return ((NumericTag) this.tags.get(local30_1)).getAsShort();
            }
        } catch (ClassCastException ignored) {
        }

        return 0;
    }

    public int getInt(String local31_1) {
        try {
            if (this.contains(local31_1, 99)) {
                return ((NumericTag) this.tags.get(local31_1)).getAsInt();
            }
        } catch (ClassCastException ignored) {
        }

        return 0;
    }

    public long getLong(String local32_1) {
        try {
            if (this.contains(local32_1, 99)) {
                return ((NumericTag) this.tags.get(local32_1)).getAsLong();
            }
        } catch (ClassCastException ignored) {
        }

        return 0L;
    }

    public float getFloat(String local33_1) {
        try {
            if (this.contains(local33_1, 99)) {
                return ((NumericTag) this.tags.get(local33_1)).getAsFloat();
            }
        } catch (ClassCastException ignored) {
        }

        return 0.0F;
    }

    public double getDouble(String local34_1) {
        try {
            if (this.contains(local34_1, 99)) {
                return ((NumericTag) this.tags.get(local34_1)).getAsDouble();
            }
        } catch (ClassCastException ignored) {
        }

        return 0.0D;
    }

    public String getString(String local35_1) {
        try {
            if (this.contains(local35_1, 8)) {
                return this.tags.get(local35_1).getAsString();
            }
        } catch (ClassCastException ignored) {
        }

        return "";
    }

    public byte[] getByteArray(String local36_1) {
        try {
            if (this.contains(local36_1, 7)) {
                return ((ByteArrayTag) this.tags.get(local36_1)).getAsByteArray();
            }
        } catch (ClassCastException var3) {
            var3.printStackTrace();
        }

        return new byte[0];
    }

    public int[] getIntArray(String local37_1) {
        try {
            if (this.contains(local37_1, 11)) {
                return ((IntArrayTag) this.tags.get(local37_1)).getAsIntArray();
            }
        } catch (ClassCastException var3) {
            var3.printStackTrace();
        }

        return new int[0];
    }

    public long[] getLongArray(String local38_1) {
        try {
            if (this.contains(local38_1, 12)) {
                return ((LongArrayTag) this.tags.get(local38_1)).getAsLongArray();
            }
        } catch (ClassCastException var3) {
            var3.printStackTrace();
        }

        return new long[0];
    }

    public CompoundTag getCompound(String local39_1) {
        try {
            if (this.contains(local39_1, 10)) {
                return (CompoundTag) this.tags.get(local39_1);
            }
        } catch (ClassCastException var3) {
            var3.printStackTrace();
        }

        return new CompoundTag();
    }

    public ListTag getList(String local40_1, int local40_2) {
        try {
            if (this.getTagType(local40_1) == 9) {
                ListTag local40_3 = (ListTag) this.tags.get(local40_1);
                if (!local40_3.isEmpty() && local40_3.getElementType() != local40_2) {
                    return new ListTag();
                }

                return local40_3;
            }
        } catch (ClassCastException var4) {
            var4.printStackTrace();
        }

        return new ListTag();
    }

    public boolean getBoolean(String local41_1) {
        return this.getByte(local41_1) != 0;
    }

    public void remove(String local42_1) {
        this.tags.remove(local42_1);
    }

    public String toString() {
        StringBuilder local43_1 = new StringBuilder("{");
        Collection<String> local43_2 = this.tags.keySet();
        if (LOGGER.isDebugEnabled()) {
            var local43_3 = Lists.newArrayList(this.tags.keySet());
            Collections.sort(local43_3);
            local43_2 = local43_3;
        }
        for (var o : local43_2) {
            if (local43_1.length() != 1) local43_1.append(',');
            local43_1.append(handleEscape(o)).append(':').append(this.tags.get(o));
        }
        return local43_1.append('}').toString();
    }

    public boolean isEmpty() {
        return this.tags.isEmpty();
    }

    public CompoundTag copy() {
        var local46_1 = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
        return new CompoundTag(local46_1);
    }

    public boolean equals(Object local47_1) {
        if (this == local47_1) {
            return true;
        } else {
            return local47_1 instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag) local47_1).tags);
        }
    }

    public int hashCode() {
        return this.tags.hashCode();
    }

    private static void writeNamedTag(String local49_0, Tag local49_1, DataOutput local49_2) throws IOException {
        local49_2.writeByte(local49_1.getId());
        if (local49_1.getId() != 0) {
            local49_2.writeUTF(local49_0);
            local49_1.write(local49_2);
        }
    }

    private static byte readNamedTagType(DataInput local50_0, SizeFence local50_1) throws IOException {
        return local50_0.readByte();
    }

    private static String readNamedTagName(DataInput local51_0, SizeFence local51_1) throws IOException {
        return local51_0.readUTF();
    }

    private static Tag readNamedTagData(TagType<?> local52_0, String local52_1, DataInput local52_2, int local52_3,
                                        SizeFence local52_4) {
        try {
            return local52_0.load(local52_2, local52_3, local52_4);
        } catch (IOException var8) {
            var8.printStackTrace();
            throw new RuntimeException(var8);
        }
    }

    public CompoundTag merge(CompoundTag local53_1) {

        for (String local53_2 : local53_1.tags.keySet()) {
            Tag local53_3 = local53_1.tags.get(local53_2);
            if (local53_3.getId() == 10) {
                if (this.contains(local53_2, 10)) {
                    CompoundTag local53_4 = this.getCompound(local53_2);
                    local53_4.merge((CompoundTag) local53_3);
                } else {
                    this.put(local53_2, local53_3.copy());
                }
            } else {
                this.put(local53_2, local53_3.copy());
            }
        }

        return this;
    }

    protected static String handleEscape(String local54_0) {
        return SIMPLE_VALUE.matcher(local54_0).matches() ? local54_0 : StringTag.quoteAndEscape(local54_0);
    }
}
