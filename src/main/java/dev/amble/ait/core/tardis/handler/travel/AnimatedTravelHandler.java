package dev.amble.ait.core.tardis.handler.travel;

import dev.amble.ait.client.tardis.manager.ClientTardisManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import dev.amble.ait.core.blockentities.ExteriorBlockEntity;
import dev.amble.ait.core.tardis.animation.v2.AnimationHolder;
import dev.amble.ait.data.Exclude;

import java.util.UUID;

public abstract class AnimatedTravelHandler extends ProgressiveTravelHandler {

    private int animationTicks;

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(AnimationHolder.UPDATE_PACKET, (client, handler, buf, responseSender) -> {
            TravelHandlerBase.State state = buf.readEnumConstant(TravelHandlerBase.State.class);
            UUID uuid = buf.readUuid();

            ClientTardisManager.getInstance().getTardis(uuid, tardis -> {

                tardis.travel().getAnimations().onStateChange(state);
            });
        });
    }

    @Exclude
    private AnimationHolder animations;

    public AnimatedTravelHandler(Id id) {
        super(id);
    }

    @Override
    public void onLoaded() {
        super.onLoaded();

        this.state.addListener(state -> this.getAnimations().onStateChange(state));
    }

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);

        State state = this.getState();

        if (this.shouldTickAnimation()) {
            this.tickAnimationProgress(server, state);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void tick(MinecraftClient client) {
        super.tick(client);

        if (this.shouldTickAnimation()) {
            this.getAnimations().tick(client);
        }
    }

    protected void tickAnimationProgress(MinecraftServer server, State state) {
        this.getAnimations().tick(server);

        if (!this.getAnimations().isAged()) return;

        if (this instanceof TravelHandler handler)
            state.finish(handler);
    }

    public float getAlpha() {
        return this.getAnimations().getAlpha();
    }

    protected AnimationHolder getAnimations() {
        if (this.animations == null) {
            this.animations = new AnimationHolder(this.tardis());
            this.animations.onStateChange(this.getState());
        }

        return this.animations;
    }

    public int getAnimTicks() {
        return animationTicks; // TODO
    }

    public int getMaxAnimTicks() {
        return tardis.stats().getTravelEffects().get(this.getState()).length(); // TODO
    }

    public abstract boolean shouldTickAnimation();
}
