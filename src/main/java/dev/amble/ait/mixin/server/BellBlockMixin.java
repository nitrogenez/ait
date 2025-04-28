package dev.amble.ait.mixin.server;

import dev.amble.ait.core.AITSounds;
import dev.amble.ait.core.item.SonicItem;
import dev.amble.ait.core.item.sonic.SonicMode;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BellBlock.class)
public class BellBlockMixin {

    @Inject(method = "onUse", at = @At(value = "HEAD"))
    private void ait$onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient()) {
            Item itemInHand = player.getStackInHand(hand).getItem();

            // sonic summon used on bell (locator feature) -> do not ignore sonic use
            if (!player.isSneaking() && itemInHand instanceof SonicItem) {
                itemInHand.use(world, player, hand);
            }
        }
    }

    @Inject(method = "ring(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"), cancellable = true)
    public void ait$playSound(Entity entity, World world, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemInHand = ((PlayerEntity) entity).getActiveItem();

        // sonic summon used on bell (locator feature) -> play cloister sound
        if (entity instanceof PlayerEntity
                && itemInHand.getItem() instanceof SonicItem
                && SonicItem.mode(itemInHand) == SonicMode.Modes.SCANNING)
        {
            world.playSound(null, pos, AITSounds.CLOISTER, SoundCategory.BLOCKS, 2.0F, 1.0F);
            cir.cancel();
        }
    }
}
