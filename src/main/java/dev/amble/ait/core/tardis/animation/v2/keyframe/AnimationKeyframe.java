package dev.amble.ait.core.tardis.animation.v2.keyframe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.amble.ait.core.tardis.handler.travel.TravelHandlerBase;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;

import dev.amble.ait.api.tardis.Disposable;
import dev.amble.ait.api.tardis.TardisTickable;

/**
 * Represents a keyframe in an animation.
 * The keyframe interpolates between two alpha values over a duration of ticks.
 */
public class AnimationKeyframe implements TardisTickable, Disposable {
    public static final Codec<AnimationKeyframe> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Codecs.NONNEGATIVE_INT.fieldOf("duration").forGetter(AnimationKeyframe::getDuration),
                Codec.FLOAT.fieldOf("alpha").forGetter(AnimationKeyframe::getTargetAlpha),
                Interpolation.CODEC.optionalFieldOf("interpolation", Interpolation.CUBIC).forGetter(AnimationKeyframe::getInterpolation)
            ).apply(instance, AnimationKeyframe::new)
    );

    protected final int duration;
    protected final float targetAlpha;
    protected final Interpolation interpolation;

    protected int ticks;

    private float startingAlpha;

    /**
     * @param duration The duration of the keyframe in ticks.
     * @param alpha The target alpha value of the keyframe.
     * @param sAlpha The starting alpha value of the keyframe.
     */
    public AnimationKeyframe(int duration, float alpha, float sAlpha, Interpolation interpolation) {
        if (duration < 0 || alpha < 0 || sAlpha < 0 || sAlpha > 1) {
            throw new IllegalArgumentException("Invalid keyframe parameters: " + duration + ", " + alpha + ", " + sAlpha);
        }

        this.duration = duration;
        this.targetAlpha = alpha;
        this.startingAlpha = sAlpha;

        this.interpolation = interpolation;

        this.ticks = 0;
    }

    public AnimationKeyframe(int duration, float alpha, Interpolation interpolation) {
        this(duration, alpha, 0, interpolation);
    }

    public boolean isDone() {
        return ticks >= duration;
    }

    public void tickCommon(boolean isClient) {
        ticks++;
    }

    public void setStartingAlpha(float sAlpha) {
        this.startingAlpha = sAlpha;
    }

    @Override
    public void dispose() {
        this.ticks = 0;
        this.startingAlpha = 0;
    }

    @Override
    public boolean isAged() {
        return this.isDone();
    }

    @Override
    public void age() {
        this.ticks = this.duration;
    }

    public int ticks() {
        return this.ticks;
    }

    public float getAlpha() {
        return this.calculateAlpha();
    }

    private int getDuration() {
        return this.duration;
    }

    private float getTargetAlpha() {
        return this.targetAlpha;
    }

    private Interpolation getInterpolation() {
        return this.interpolation;
    }

    /**
     * Catmull-Rom (CUBIC) interpolation between startingAlpha and targetAlpha based off ticks and duration
     * @return The interpolated alpha value.
     */
    protected float calculateAlpha() {
        float progress = (float) this.ticks / this.duration;

        return this.getInterpolation().interpolate(progress, this.startingAlpha, this.targetAlpha);
    }

    public AnimationKeyframe instantiate() {
        return new AnimationKeyframe(this.duration, this.targetAlpha, this.startingAlpha, this.interpolation);
    }

    public enum Interpolation {
        LINEAR {
            @Override
            public float interpolate(float progress, float start, float end) {
                return MathHelper.lerp(progress, start, end);
            }
        },
        CUBIC {;
            @Override
            public float interpolate(float progress, float start, float end) {
                return MathHelper.catmullRom(progress, start, start, end, end);
            }
        };

        public abstract float interpolate(float progress, float start, float end);

        public static final Codec<Interpolation> CODEC = Codecs.NON_EMPTY_STRING.flatXmap(s -> {
            try {
                return DataResult.success(Interpolation.valueOf(s.toUpperCase()));
            } catch (Exception e) {
                return DataResult.error(() -> "Invalid state: " + s + "! | " + e.getMessage());
            }
        }, var -> DataResult.success(var.toString()));
    }
}
