package dev.amble.ait.core.tardis.handler.travel;

import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.joml.Vector3f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import dev.amble.ait.AITMod;
import dev.amble.ait.client.tardis.manager.ClientTardisManager;
import dev.amble.ait.core.tardis.animation.v2.AnimationHolder;
import dev.amble.ait.core.tardis.animation.v2.TardisAnimation;
import dev.amble.ait.core.tardis.animation.v2.datapack.TardisAnimationRegistry;
import dev.amble.ait.data.Exclude;
import dev.amble.ait.data.properties.Property;
import dev.amble.ait.data.properties.Value;

public abstract class AnimatedTravelHandler extends ProgressiveTravelHandler {
    private static final Identifier DEFAULT_DEMAT = AITMod.id("new_demat");
    private static final Identifier DEFAULT_MAT = AITMod.id("new_mat");

    private static final Property<Identifier> DEMAT_FX = new Property<>(Property.Type.IDENTIFIER, "demat_fx", DEFAULT_DEMAT);
    private static final Property<Identifier> MAT_FX = new Property<>(Property.Type.IDENTIFIER, "mat_fx", DEFAULT_MAT);
    private final Value<Identifier> dematId = DEMAT_FX.create(this);
    private final Value<Identifier> matId = MAT_FX.create(this);

    @Exclude
    private boolean isAnimationInvalidated;

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(AnimationHolder.UPDATE_PACKET, (client, handler, buf, responseSender) -> {
            State state = buf.readEnumConstant(State.class);
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

        dematId.of(this, DEMAT_FX);
        matId.of(this, MAT_FX);

        dematId.addListener((id) -> {
            isAnimationInvalidated = true;
        });
        matId.addListener((id) -> {
            isAnimationInvalidated = true;
        });
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

        if (this.isAnimationInvalidated) {
            this.animations = null;
        }
    }

    public float getAlpha() {
        return this.getAnimations().getAlpha();
    }

    public Vector3f getScale() {
        return this.getAnimations().getScale();
    }

    public Vector3f getAnimationPosition() {
        return this.getAnimations().getPosition();
    }

    public Vector3f getAnimationRotation() {
        return this.getAnimations().getRotation();
    }

    protected AnimationHolder getAnimations() {
        if (this.animations == null) {
            this.animations = new AnimationHolder(this.tardis());
            this.animations.onStateChange(this.getState());
        }

        return this.animations;
    }

    public Identifier getAnimationIdFor(State state) {
        return switch (state) {
            case LANDED, FLIGHT -> Identifier.of("", "");
            case DEMAT -> this.dematId.get();
            case MAT -> this.matId.get();
        };
    }

    protected TardisAnimation getAnimationFor(State state) {
        return TardisAnimationRegistry.getInstance().getOrFallback(this.getAnimationIdFor(state));
    }

    public void setAnimationFor(State state, Identifier id) {
        switch (state) {
            case LANDED, FLIGHT -> {}
            case DEMAT -> this.dematId.set(id);
            case MAT -> this.matId.set(id);
        }
    }

    public abstract boolean shouldTickAnimation();
}
