package dev.amble.ait.core.entities;

import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import dev.amble.ait.core.AITEntityTypes;
import dev.amble.ait.core.AITItems;
import dev.amble.ait.module.planet.core.util.ISpaceImmune;

public class GallifreyFallsPaintingEntity extends AbstractDecorationEntity implements ISpaceImmune {
    private static final int WIDTH = 48;
    private static final int HEIGHT = 32;

    public GallifreyFallsPaintingEntity(EntityType<? extends GallifreyFallsPaintingEntity> entityType, World world) {
        super(AITEntityTypes.GALLIFREY_FALLS_PAINTING_TYPE, world);
    }

    private GallifreyFallsPaintingEntity(World world, BlockPos pos) {
        super(AITEntityTypes.GALLIFREY_FALLS_PAINTING_TYPE, world, pos);
    }

    public static Optional<GallifreyFallsPaintingEntity> placePainting(World world, BlockPos pos, Direction facing) {
        GallifreyFallsPaintingEntity paintingEntity = new GallifreyFallsPaintingEntity(world, pos);

        paintingEntity.setFacing(facing);

        if (paintingEntity.canStayAttached()) {
            return Optional.of(paintingEntity);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int getWidthPixels() {
        return WIDTH;
    }

    @Override
    public int getHeightPixels() {
        return HEIGHT;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(AITItems.GALLIFREY_FALLS_PAINTING);
    }

    @Override
    public void onBreak(@Nullable Entity entity) {
        if (!this.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            return;
        }
        this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0f, 1.0f);
        if (entity instanceof PlayerEntity player && player.isCreative()) {
            return;
        }
        this.dropItem(AITItems.GALLIFREY_FALLS_PAINTING);
    }

    @Override
    public void onPlace() {
        this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0f, 1.0f);
    }
}
