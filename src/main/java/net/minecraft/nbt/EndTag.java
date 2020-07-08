package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag implements Tag {
    public static final TagType<EndTag> TYPE = new TagType<>() {
        public EndTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) {
            local1_3.accountBits(64L);
            return EndTag.INSTANCE;
        }

        public String getName() {
            return "END";
        }

        public String getPrettyName() {
            return "TAG_End";
        }

        public boolean isValue() {
            return true;
        }
    };
    public static final EndTag INSTANCE = new EndTag();

    private EndTag() {
    }

    public void write(DataOutput local1_1) {
    }

    public byte getId() {
        return 0;
    }

    public TagType<EndTag> getType() {
        return TYPE;
    }

    public String toString() {
        return "END";
    }

    public EndTag copy() {
        return this;
    }

}
