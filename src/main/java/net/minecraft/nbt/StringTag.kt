package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class StringTag implements Tag {
    public static final TagType<StringTag> TYPE = new TagType<>() {
        public StringTag load(DataInput local1_1, int local1_2, NbtAccounter local1_3) throws IOException {
            local1_3.accountBits(288L);
            String local1_4 = local1_1.readUTF();
            local1_3.accountBits(16 * local1_4.length());
            return StringTag.valueOf(local1_4);
        }

        public String getName() {
            return "STRING";
        }

        public String getPrettyName() {
            return "TAG_String";
        }

        public boolean isValue() {
            return true;
        }
    };
    private static final StringTag EMPTY = new StringTag("");
    private final String data;

    private StringTag(String local0_1) {
        Objects.requireNonNull(local0_1, "Null string not allowed");
        this.data = local0_1;
    }

    public static StringTag valueOf(String local1_0) {
        return local1_0.isEmpty() ? EMPTY : new StringTag(local1_0);
    }

    public void write(DataOutput local2_1) throws IOException {
        local2_1.writeUTF(this.data);
    }

    public byte getId() {
        return 8;
    }

    public TagType<StringTag> getType() {
        return TYPE;
    }

    public String toString() {
        return quoteAndEscape(this.data);
    }

    public StringTag copy() {
        return this;
    }

    public boolean equals(Object local7_1) {
        if (this == local7_1) {
            return true;
        } else {
            return local7_1 instanceof StringTag && Objects.equals(this.data, ((StringTag) local7_1).data);
        }
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    public String getAsString() {
        return this.data;
    }

    public static String quoteAndEscape(String local11_0) {
        StringBuilder local11_1 = new StringBuilder(" ");
        char local11_2 = 0;

        for (int local11_3 = 0; local11_3 < local11_0.length(); ++local11_3) {
            char local11_4 = local11_0.charAt(local11_3);
            if (local11_4 == '\\') {
                local11_1.append('\\');
            } else if (local11_4 == '"' || local11_4 == '\'') {
                if (local11_2 == 0) {
                    local11_2 = local11_4 == '"' ? (char) 39 : 34;
                }

                if (local11_2 == local11_4) {
                    local11_1.append('\\');
                }
            }

            local11_1.append(local11_4);
        }

        if (local11_2 == 0) {
            local11_2 = 34;
        }

        local11_1.setCharAt(0, local11_2);
        local11_1.append(local11_2);
        return local11_1.toString();
    }
}
