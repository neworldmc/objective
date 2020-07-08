package net.minecraft.nbt;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NbtIo {
    public static CompoundTag readCompressed(InputStream local0_0) throws IOException {
        try (var local0_1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(local0_0)))) {
            return read(local0_1, NbtAccounter.UNLIMITED);
        }
    }

    public static void writeCompressed(CompoundTag local1_0, OutputStream local1_1) throws IOException {
        try (var local1_2 = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(local1_1)))) {
            write(local1_0, local1_2);
        }
    }

    public static CompoundTag read(DataInputStream local2_0) throws IOException {
        return read(local2_0, NbtAccounter.UNLIMITED);
    }

    public static CompoundTag read(DataInput local3_0, NbtAccounter local3_1) throws IOException {
        Tag local3_2 = readUnnamedTag(local3_0, 0, local3_1);
        if (local3_2 instanceof CompoundTag) {
            return (CompoundTag) local3_2;
        } else {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(CompoundTag local4_0, DataOutput local4_1) throws IOException {
        writeUnnamedTag(local4_0, local4_1);
    }

    private static void writeUnnamedTag(Tag local5_0, DataOutput local5_1) throws IOException {
        local5_1.writeByte(local5_0.getId());
        if (local5_0.getId() != 0) {
            local5_1.writeUTF("");
            local5_0.write(local5_1);
        }
    }

    private static Tag readUnnamedTag(DataInput local6_0, int local6_1, NbtAccounter local6_2) throws IOException {
        byte local6_3 = local6_0.readByte();
        if (local6_3 == 0) {
            return EndTag.INSTANCE;
        } else {
            local6_0.readUTF();

            try {
                return TagTypes.getType(local6_3).load(local6_0, local6_1, local6_2);
            } catch (IOException var7) {
                var7.printStackTrace();
                throw new RuntimeException(var7);
            }
        }
    }
}
