package net.minecraft.nbt;

public class NbtAccounter {
    public static final NbtAccounter UNLIMITED = new NbtAccounter(0L) {
        public void accountBits(long local1_1) {
        }
    };
    private final long quota;
    private long usage;

    public NbtAccounter(long local0_1) {
        this.quota = local0_1;
    }

    public void accountBits(long local1_1) {
        this.usage += local1_1 / 8L;
        if (this.usage > this.quota) {
            throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + this.usage +
                    "bytes where max allowed: " + this.quota);
        }
    }
}
