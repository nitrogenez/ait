package mdteam.ait.core.item;

import mdteam.ait.api.tardis.ArtronHolder;
import mdteam.ait.api.tardis.ArtronHolderItem;
import mdteam.ait.api.tardis.LinkableItem;
import mdteam.ait.core.AITSounds;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.core.managers.RiftChunkManager;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.animation.ExteriorAnimation;
import mdteam.ait.tardis.data.TardisCrashData;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.util.FlightUtil;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SonicItem extends LinkableItem implements ArtronHolderItem {
    public static final double MAX_FUEL = 1000;

    public static final String MODE_KEY = "mode";
    public static final String PREV_MODE_KEY = "PreviousMode";
    public static final String INACTIVE = "inactive";

    private static final int SONIC_SFX_LENGTH = FlightUtil.convertSecondsToTicks(1.5);

    public SonicItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        BlockPos pos = user.getBlockPos();

        boolean success = useSonic(world, user, pos, hand, stack);

        return success ? TypedActionResult.consume(stack) : TypedActionResult.fail(stack);
    }

    // fixme no me gusta nada
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (player == null)
            return ActionResult.FAIL;

        boolean success = useSonic(world, player, pos, context.getHand(), stack);

        return success ? ActionResult.CONSUME : ActionResult.FAIL;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        setPreviousMode(stack);
        setMode(stack, Mode.INACTIVE);

        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        setPreviousMode(stack);
        setMode(stack, Mode.INACTIVE);

        return super.finishUsing(stack, world, user);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);

        if (!(user instanceof PlayerEntity player)) {
            return;
        }

        System.out.println(remainingUseTicks % SONIC_SFX_LENGTH);

        if (remainingUseTicks % SONIC_SFX_LENGTH != 0) return;

        playSonicSounds(player);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000; // prolly big enough
    }

    private boolean useSonic(World world, PlayerEntity user, BlockPos pos, Hand hand, ItemStack stack) {
        Tardis tardis = getTardis(stack);
        boolean hasTardis = tardis != null;
        NbtCompound nbt = stack.getOrCreateNbt();
        Mode mode = findMode(stack);

        if (world.isClient()) return true;

        // if (user.isUsingItem() && stack.equals(user.getActiveItem())) return false;

        if (this.isOutOfFuel(stack)) return false;

        if (user.isSneaking()) {
            world.playSound(null, user.getBlockPos(), AITSounds.SONIC_SWITCH, SoundCategory.PLAYERS, 1f, 1f);
            cycleMode(stack);

            this.removeFuel(stack);

            return true;
        }

        if (mode == Mode.INACTIVE) {
            Mode prev = findPreviousMode(stack);
            if (prev == Mode.INACTIVE) return false;

            setMode(stack, prev);
            mode = findMode(stack);
        }

        // todo fix issues with this
        if (mode == Mode.OVERLOAD) { // fixme should be in "run" in Overload mode
            if (!hasTardis) return false;

            TardisCrashData crash = tardis.getHandlers().getCrashData();
            boolean isToxic = crash.isToxic();
            boolean isUnstable = crash.isUnstable();
            int repairTicks = crash.getRepairTicks();

            if (!isToxic && !isUnstable) return false;

            if (tardis.getDoor().isClosed()) {
                user.sendMessage(Text.literal("Doors need to be open for repair!").formatted(Formatting.RED), true);
                return true;
            }

            if (world != TardisUtil.getTardisDimension()) return false;

            crash.setRepairTicks(repairTicks <= 0 ? 0 : repairTicks - 20);
            user.sendMessage(Text.literal("Repairing: " + crash.getRepairTicks()).formatted(Formatting.GOLD), true);
            return true;
        }

        user.setCurrentHand(hand);

        this.removeFuel(stack);

        mode.run(tardis, world, pos, user, stack);

        return true;
    }

    public static Tardis getTardis(ItemStack item) {
        NbtCompound nbt = item.getOrCreateNbt();

        if (!nbt.contains("tardis")) return null;

        return ServerTardisManager.getInstance().getTardis(UUID.fromString(nbt.getString("tardis")));
    }

    @Override
    public void link(ItemStack stack, UUID uuid) {
        super.link(stack, uuid);

        NbtCompound nbt = stack.getOrCreateNbt();

        nbt.putInt(MODE_KEY, 0);
        nbt.putBoolean(INACTIVE, true);
    }
    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = new ItemStack(this);
        NbtCompound nbt = stack.getOrCreateNbt();

        nbt.putInt(MODE_KEY, 0);
        nbt.putDouble(FUEL_KEY, getMaxFuel(stack));

        return stack;
    }
    public static void playSonicSounds(PlayerEntity player) {
//        player.getWorld().playSoundFromEntity(null, player, AITSounds.SONIC_USE, SoundCategory.PLAYERS, 1f, (-player.getPitch() / 90f) + 1f);
        player.getWorld().playSoundFromEntity(null, player, AITSounds.SONIC_USE, SoundCategory.PLAYERS, 1f, 1f);
    }

    public static void cycleMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();

        if (!(nbt.contains(PREV_MODE_KEY))) {
            setMode(stack, 0);
            setPreviousMode(stack);
        }

        SonicItem.setMode(stack, nbt.getInt(PREV_MODE_KEY) + 1 <= Mode.values().length - 1 ? nbt.getInt(PREV_MODE_KEY) + 1 : 0);
        setPreviousMode(stack);
    }

    public static int findModeInt(ItemStack stack) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        if (!nbtCompound.contains(MODE_KEY))
            return 0;
        return nbtCompound.getInt(MODE_KEY);
    }

    public static void setMode(ItemStack stack, int mode) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        nbtCompound.putInt(MODE_KEY, mode);
    }
    public static void setMode(ItemStack stack, Mode mode) {
        setMode(stack, mode.ordinal());
    }
    private static void setPreviousMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(PREV_MODE_KEY, findModeInt(stack));
    }
    private static Mode findPreviousMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();

        if (!nbt.contains(PREV_MODE_KEY)) setPreviousMode(stack);

        return intToMode(nbt.getInt(PREV_MODE_KEY));
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    // this smells
    public static Mode intToMode(int mode) {
        return Mode.values()[mode];
    }

    // ew
    private static Mode findMode(ItemStack stack) {
        return intToMode(findModeInt(stack));
    }

    // Fuel
    @Override
    public double getMaxFuel(ItemStack stack) {
        return MAX_FUEL;
    }
    @Override
    public boolean isOutOfFuel(ItemStack stack) {
        boolean outage = ArtronHolderItem.super.isOutOfFuel(stack);

        if (outage && intToMode(stack.getOrCreateNbt().getInt(MODE_KEY)) != Mode.INACTIVE) {
            setMode(stack, Mode.INACTIVE);
        }

        return outage;
    }
    protected void removeFuel(ItemStack stack) {
        this.removeFuel(1, stack);
    }
    protected void addFuel(ItemStack stack) {
        this.addFuel(1, stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (!Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("tooltip.ait.remoteitem.holdformoreinfo").formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
            return;
        }

        NbtCompound tag = stack.getOrCreateNbt();
        String text = tag.contains("tardis") ? tag.getString("tardis").substring(0, 8)
                : Text.translatable("message.ait.sonic.none").getString();
        String position = Text.translatable("message.ait.sonic.none").getString();
        if(tag.contains("tardis")) {
            Tardis tardis = ClientTardisManager.getInstance().getTardis(UUID.fromString(tag.getString("tardis")));
            if (tardis != null)
                position = tardis.getTravel() == null || tardis.getTravel().getExteriorPos() == null ? "In Flight..." : tardis.getTravel().getExteriorPos().toShortString();
        }

        tooltip.add(Text.translatable("message.ait.sonic.mode").formatted(Formatting.BLUE));

        Mode mode = findPreviousMode(stack);
        tooltip.add(Text.literal(mode.asString()).formatted(mode.format).formatted(Formatting.BOLD));

        tooltip.add(
                Text.literal("AU: ").formatted(Formatting.BLUE)
                        .append(Text.literal(
                                String.valueOf(
                                        Math.round(this.getCurrentFuel(stack)))
                        ).formatted(Formatting.GREEN)
                    )
        ); // todo translatable + changing of colour based off fuel

        if (tag.contains("tardis")) tooltip.add(ScreenTexts.EMPTY);

        super.appendTooltip(stack, world, tooltip, context);

        if (tag.contains("tardis")) { // Adding the sonics mode
            tooltip.add(Text.literal("Position: ").formatted(Formatting.BLUE));
            tooltip.add(Text.literal("> " + position).formatted(Formatting.GRAY));
        }

        tooltip.add(ScreenTexts.EMPTY);
    }

    public enum Mode implements StringIdentifiable {
        INACTIVE(Formatting.GRAY) {
            @Override
            public void run(Tardis tardis, World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
            }
        },
        INTERACTION(Formatting.GREEN) {
            @Override
            public void run(Tardis tardis, World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
                BlockState blockState = world.getBlockState(pos);

                if (!(CampfireBlock.canBeLit(blockState) || CandleBlock.canBeLit(blockState) || CandleCakeBlock.canBeLit(blockState)))
                    return;

                world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.4f + 0.8f);
                world.setBlockState(pos, blockState.with(Properties.LIT, true), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
        },
        OVERLOAD(Formatting.RED) {
            @Override
            public void run(Tardis tardis, World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
                // fixme temporary replacement for exterior changing

                BlockEntity entity = world.getBlockEntity(pos);
                Block block = world.getBlockState(pos).getBlock();

                // fixme this doesnt work because a dispenser requires that you have redstone power input or the state wont trigger :/ - Loqor
                /*if(player.isSneaking() && block instanceof DispenserBlock dispenser) {
                    world.setBlockState(pos, world.getBlockState(pos).with(Properties.TRIGGERED, true), Block.NO_REDRAW);
                    //world.emitGameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
                }*/

                if(block instanceof TntBlock tnt) {
                    TntBlock.primeTnt(world, pos);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                }
            }
        },
        SCANNING(Formatting.AQUA) {
            @Override
            public void run(Tardis tardis, World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
                if (!(world.getRegistryKey() == World.OVERWORLD && !world.isClient())) return;

                Text found = Text.translatable("message.ait.sonic.riftfound").formatted(Formatting.AQUA).formatted(Formatting.BOLD);
                Text notfound = Text.translatable("message.ait.sonic.riftnotfound").formatted(Formatting.AQUA).formatted(Formatting.BOLD);
                player.sendMessage((RiftChunkManager.isRiftChunk(pos) ? found : notfound), true);
                if(RiftChunkManager.isRiftChunk(pos))
                    player.sendMessage(Text.literal("AU: " + (RiftChunkManager.getArtronLevels(world, pos))).formatted(Formatting.GOLD));
            }
        },
        TARDIS(Formatting.BLUE) {
            @Override
            public void run(Tardis tardis, World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
                if (tardis == null) return;
                if (world.getBlockEntity(pos) instanceof ExteriorBlockEntity exteriorBlockEntity) {
                    if (exteriorBlockEntity.findTardis().isEmpty()) return;
                    int repairticksleft = exteriorBlockEntity.findTardis().get().getHandlers().getCrashData().getRepairTicks();
                    int repairminutes = repairticksleft / 20 / 60;
                    if (repairticksleft == 0) {
                        player.sendMessage(Text.literal("Your tardis is not damaged").formatted(Formatting.GOLD), true);
                        return;
                    }
                    player.sendMessage(Text.literal("You have " + repairminutes + (repairminutes == 1 ? " minute" : " minutes") + " of repair left.").formatted(Formatting.GOLD), true);
                    return;
                }
                if (world == TardisUtil.getTardisDimension()) {
                    world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 1F, 0.2F);
                    player.sendMessage(Text.translatable("message.ait.remoteitem.warning3"), true);
                    return;
                }

                world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS);

                TardisTravel travel = tardis.getTravel();

                AbsoluteBlockPos.Directed target = new AbsoluteBlockPos.Directed(pos, world, player.getMovementDirection().getOpposite());

                if (!ExteriorAnimation.isNearTardis(player, tardis, 256)) {
                    travel.setDestination(target, true);
                    return;
                }

                FlightUtil.travelTo(tardis, target);

                player.sendMessage(Text.translatable("message.ait.sonic.handbrakedisengaged"), true);
            }
        };

        public Formatting format;

        Mode(Formatting format) {
            this.format = format;
        }

        public abstract void run(@Nullable Tardis tardis, World world, BlockPos pos, PlayerEntity player, ItemStack stack);

        @Override
        public String asString() {
            return StringUtils.capitalize(this.toString().replace("_", " "));
        }
    }
}
