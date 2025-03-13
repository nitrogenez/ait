package dev.amble.ait.core.tardis.control.impl;

import dev.amble.ait.AITMod;
import dev.amble.ait.api.link.LinkableItem;
import dev.amble.ait.core.AITSounds;
import dev.amble.ait.core.item.HammerItem;
import dev.amble.ait.core.item.HandlesItem;
import dev.amble.ait.core.item.SonicItem;
import dev.amble.ait.core.tardis.Tardis;
import dev.amble.ait.core.tardis.TardisDesktop;
import dev.amble.ait.core.tardis.control.Control;
import dev.amble.ait.core.tardis.control.sequences.SequenceHandler;
import dev.amble.ait.core.tardis.handler.ButlerHandler;
import dev.amble.ait.core.tardis.handler.ExtraHandler;
import dev.amble.ait.core.tardis.handler.SonicHandler;
import dev.drtheo.scheduler.api.Scheduler;
import dev.drtheo.scheduler.api.TimeUnit;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class HammerHangerControl extends Control {

    public HammerHangerControl() {
        super(AITMod.id("hammer_hanger"));
    }

    @Override
    public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world, BlockPos console,
                             boolean leftClick) {
        super.runServer(tardis, player, world, console, leftClick);
        ExtraHandler handler = tardis.extra();

        if ((leftClick || player.isSneaking()) && (handler.getConsoleHammer() != null)) {
            ItemStack item;

            item = handler.takeConsoleHammer();

            player.getInventory().offerOrDrop(item);
            return true;
        }

        ItemStack stack = player.getMainHandStack();

        if (stack.getItem() instanceof HammerItem) {
            handler.insertConsoleHammer(stack, console);
            player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }

        TardisDesktop.playSoundAtConsole(tardis.asServer().getInteriorWorld(), console, SoundEvents.BLOCK_CHAIN_STEP, SoundCategory.PLAYERS, 6f, 1);

        return true;

    }
}
