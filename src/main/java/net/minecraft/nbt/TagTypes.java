package net.minecraft.nbt;

public class TagTypes {
    private static final TagType<?>[] TYPES;

    public static TagType<?> getType(int local0_0) {
        return local0_0 >= 0 && local0_0 < TYPES.length ? TYPES[local0_0] : TagType.createInvalid(local0_0);
    }

    static {
        TYPES = new TagType[]{EndTag.TYPE, ByteTag.TYPE, ShortTag.TYPE, IntTag.TYPE, LongTag.TYPE, FloatTag.TYPE,
                DoubleTag.TYPE, ByteArrayTag.TYPE, StringTag.TYPE, ListTag.TYPE, CompoundTag.TYPE, IntArrayTag.TYPE,
                LongArrayTag.TYPE};
    }
}
