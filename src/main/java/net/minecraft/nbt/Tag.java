package net.minecraft.nbt;

import java.io.DataOutput;
import java.io.IOException;

public interface Tag {

    void write(DataOutput var1) throws IOException;

    String toString();

    byte getId();

    TagType<?> getType();

    Tag copy();

    default String getAsString() {
        return this.toString();
    }

}
