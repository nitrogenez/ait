package dev.amble.ait.core.tardis.animation.v2.keyframe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3f;

import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;

import dev.amble.ait.api.tardis.Disposable;
import dev.amble.ait.api.tardis.TardisTickable;
import dev.amble.ait.data.codec.MoreCodec;

/**
 * Represents a keyframe in an animation.
 * The keyframe interpolates between two alpha values over a duration of ticks.
 */
public class AnimationKeyframe implements TardisTickable, Disposable {
    public static final Codec<AnimationKeyframe> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Codecs.NONNEGATIVE_INT.fieldOf("duration").forGetter(AnimationKeyframe::getDuration),
                Codec.FLOAT.optionalFieldOf("alpha", 1f).forGetter(AnimationKeyframe::getTargetAlpha),
                MoreCodec.VECTOR3F.optionalFieldOf("scale", new Vector3f(1, 1, 1)).forGetter(AnimationKeyframe::getTargetScale),
                Interpolation.CODEC.optionalFieldOf("interpolation", Interpolation.CUBIC).forGetter(AnimationKeyframe::getInterpolation)
            ).apply(instance, AnimationKeyframe::new)
    );

    protected final int duration;
    protected final float targetAlpha;
    protected final Vector3f targetScale;
    protected final Interpolation interpolation;

    protected int ticks;

    private float startingAlpha;
    private Vector3f startingScale;

    /**
     * @param duration The duration of the keyframe in ticks.
     * @param alpha The target alpha value of the keyframe.
     * @param scales The scale to reach at the end of the keyframe.
     * @param interpolation Interpolation type to use for the keyframe.
     */
    public AnimationKeyframe(int duration, float alpha, Vector3f scales, Interpolation interpolation) {
        if (duration < 0 || alpha < 0) {
            throw new IllegalArgumentException("Invalid keyframe parameters: " + duration + ", " + alpha + ", " + scales + ", " + interpolation);
        }

        this.duration = duration;
        this.targetAlpha = alpha;

        this.targetScale = scales;

        this.interpolation = interpolation;

        this.ticks = 0;
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

    public void setStartingScale(Vector3f sScale) {
        this.startingScale = sScale;
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

    public Vector3f getScale() {
        return this.calculateScale();
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

    private Vector3f getTargetScale() {
        return this.targetScale;
    }

    /**
     * interpolation between startingAlpha and targetAlpha based off ticks and duration
     * @return The interpolated alpha value.
     */
    protected float calculateAlpha() {
        float progress = (float) this.ticks / this.duration;

        return this.getInterpolation().interpolate(progress, this.startingAlpha, this.targetAlpha);
    }

    protected Vector3f calculateScale() {
        float progress = (float) this.ticks / this.duration;

        return new Vector3f(
            this.getInterpolation().interpolate(progress, this.startingScale.x, this.targetScale.x),
            this.getInterpolation().interpolate(progress, this.startingScale.y, this.targetScale.y),
            this.getInterpolation().interpolate(progress, this.startingScale.z, this.targetScale.z)
        );
    }

    public AnimationKeyframe instantiate() {
        AnimationKeyframe created = new AnimationKeyframe(this.duration, this.targetAlpha, this.targetScale, this.interpolation);

        created.startingScale = this.startingScale;
        created.startingAlpha = this.startingAlpha;

        return created;
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
