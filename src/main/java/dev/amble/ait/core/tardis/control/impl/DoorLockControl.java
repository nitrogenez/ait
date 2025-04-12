package dev.amble.ait.core.tardis.control.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;

import dev.amble.ait.AITMod;
import dev.amble.ait.core.AITSounds;
import dev.amble.ait.core.tardis.Tardis;
import dev.amble.ait.core.tardis.control.Control;
import dev.amble.ait.data.schema.console.variant.renaissance.*;

public class DoorLockControl extends Control {
    public DoorLockControl() {
        super(AITMod.id("door_lock"));
    }

    @Override
    public Result runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world, BlockPos console, boolean leftClick) {
        super.runServer(tardis, player, world, console, leftClick);

        if (player.getRandom().nextFloat() < 0.005f) {
            world.playSound(null, console, AITSounds.CHICKEN_JOCKIE, SoundCategory.BLOCKS, 1.5f, 1.0f);
        }

        tardis.door().interactToggleLock(player);
        return tardis.door().locked() ? Result.SUCCESS_ALT : Result.SUCCESS;
    }

    @Override
    public SoundEvent getFallbackSound() {
        return AITSounds.DOOR_LOCK;
    }
}
