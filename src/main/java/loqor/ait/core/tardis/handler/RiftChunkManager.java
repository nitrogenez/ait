package loqor.ait.tardis.handler;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;

import loqor.ait.AITMod;
import loqor.ait.core.events.ServerChunkEvents;
import loqor.ait.data.DirectedGlobalPos;

@SuppressWarnings("UnstableApiUsage")
public record RiftChunkManager(ServerWorld world) {

    private static final AttachmentType<Double> ARTRON = AttachmentRegistry.createPersistent(
            new Identifier(AITMod.MOD_ID, "artron"), Codec.DOUBLE
    );

    private static final AttachmentType<Double> MAX_ARTRON = AttachmentRegistry.createPersistent(
            new Identifier(AITMod.MOD_ID, "max_artron"), Codec.DOUBLE
    );

    public static void init() {
        ServerChunkEvents.TICK.register((world, chunk) -> {
            if (world.getServer().getTicks() % 20 != 0)
                return;

            RiftChunkManager manager = RiftChunkManager.getInstance(world);
            ChunkPos pos = chunk.getPos();

            if (!manager.isRiftChunk(pos))
                return;

            if (manager.getMaxArtron(pos) < manager.getArtron(pos))
                manager.addFuel(chunk.getPos(), 1);
        });
    }

    public static RiftChunkManager getInstance(ServerWorld world) {
        return new RiftChunkManager(world);
    }

    public double getArtron(ChunkPos pos) {
        if (!this.isRiftChunk(pos))
            return 0;

        return this.world.getChunk(pos.x, pos.z).getAttachedOrCreate(ARTRON,
                () -> (double) world.getRandom().nextBetween(100, 800));
    }

    public double getMaxArtron(ChunkPos pos) {
        if (!this.isRiftChunk(pos))
            return 0;

        return this.world.getChunk(pos.x, pos.z).getAttachedOrCreate(MAX_ARTRON,
                () -> (double) world.getRandom().nextBetween(300, 1000));
    }

    public double removeFuel(ChunkPos pos, double amount) {
        if (!this.isRiftChunk(pos))
            return 0;

        double artron = this.getArtron(pos);
        artron -= artron < amount ? 0 : amount;

        this.world.getChunk(pos.x, pos.z).setAttached(ARTRON, artron);
        return artron - amount;
    }

    public void addFuel(ChunkPos pos, double amount) {
        if (!this.isRiftChunk(pos))
            return;

        RiftChunkManager.addFuel(this.world, pos, amount);
    }

    public void setCurrentFuel(ChunkPos pos, double amount) {
        this.world.getChunk(pos.x, pos.z).modifyAttached(ARTRON, d -> amount);
    }

    public boolean isRiftChunk(ChunkPos chunkPos) {
        return RiftChunkManager.isRiftChunk(this.world, chunkPos);
    }

    public boolean isRiftChunk(BlockPos pos) {
        return RiftChunkManager.isRiftChunk(world, pos);
    }

    public static boolean isRiftChunk(DirectedGlobalPos.Cached cached) {
        return isRiftChunk(cached.getWorld(), cached.getPos());
    }

    public static boolean isRiftChunk(ServerWorld world, BlockPos pos) {
        return isRiftChunk(world, new ChunkPos(pos));
    }

    public static boolean isRiftChunk(ServerWorld world, ChunkPos pos) {
        return ChunkRandom.getSlimeRandom(pos.x, pos.z,
                world.getSeed(), 987234910L
        ).nextInt(8) == 0;
    }

    private static void addFuel(ServerWorld world, ChunkPos pos, double amount) {
        world.getChunk(pos.x, pos.z).modifyAttached(ARTRON, d -> d + amount);
    }

    public static double getFuel(ServerWorld world, ChunkPos pos) {
        if (!isRiftChunk(world, pos))
            return 0;

        return world.getChunk(pos.x, pos.z).getAttachedOrCreate(ARTRON,
                () -> (double) world.getRandom().nextBetween(100, 800));
    }

    public static double getMaxFuel(ServerWorld world, ChunkPos pos) {
        if (!isRiftChunk(world, pos))
            return 0;

        return world.getChunk(pos.x, pos.z).getAttachedOrCreate(MAX_ARTRON,
                () -> (double) world.getRandom().nextBetween(300, 1000));
    }
}