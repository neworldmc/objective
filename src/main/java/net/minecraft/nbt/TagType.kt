package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag> {
    T load(DataInput var1, int var2, NbtAccounter var3) throws IOException;

    default boolean isValue() {
        return false;
    }

    String getName();

    String getPrettyName();

    static TagType<EndTag> createInvalid(final int local4_0) {
        return new TagType<>() {
            public EndTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) {
                throw new IllegalArgumentException("Invalid tag id: " + local4_0);
            }

            public String getName() {
                return "INVALID[" + local4_0 + "]";
            }

            public String getPrettyName() {
                return "UNKNOWN_" + local4_0;
            }
        };
    }
}
