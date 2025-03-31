package dev.amble.ait.core.tardis.handler.travel;

import net.minecraft.server.MinecraftServer;

import dev.amble.ait.core.blockentities.ExteriorBlockEntity;
import dev.amble.ait.core.tardis.animation.v2.AnimationHolder;
import dev.amble.ait.data.Exclude;

public abstract class AnimatedTravelHandler extends ProgressiveTravelHandler {

    private int animationTicks;

    @Exclude
    private AnimationHolder animations;

    public AnimatedTravelHandler(Id id) {
        super(id);
    }

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);

        State state = this.getState();

        if (this.shouldTickAnimation())
            this.tickAnimationProgress(state);
    }

    protected void tickAnimationProgress(State state) {
        ExteriorBlockEntity be = this.tardis().getExterior().findExteriorBlock().orElse(null);

        if (be == null)
            return;

        if (!be.isLinked()) return;

        if (!be.getAnimations().isAged()) return;

        /*
        if (this.animationTicks++ < tardis.stats().getTravelEffects().get(state).length())
            return;

        this.animationTicks = 0;*/

        if (this instanceof TravelHandler handler)
            state.finish(handler);
    }

    public float getAlpha() {
        return this.getAnimations().getAlpha();
    }

    protected AnimationHolder getAnimations() {
        if (this.animations == null) {
            this.animations = new AnimationHolder(this.tardis());
        }

        return this.animations;
    }

    public int getAnimTicks() {
        return animationTicks;
    }

    public int getMaxAnimTicks() {
        return tardis.stats().getTravelEffects().get(this.getState()).length();
    }

    public abstract boolean shouldTickAnimation();
}
