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
                MoreCodec.VECTOR3F.optionalFieldOf("position", new Vector3f(0, 0, 0)).forGetter(AnimationKeyframe::getTargetPosition),
                MoreCodec.VECTOR3F.optionalFieldOf("rotation", new Vector3f(0, 0, 0)).forGetter(AnimationKeyframe::getTargetRotation),
                MoreCodec.VECTOR3F.optionalFieldOf("scale", new Vector3f(1, 1, 1)).forGetter(AnimationKeyframe::getTargetScale),
                Interpolation.CODEC.optionalFieldOf("interpolation", Interpolation.CUBIC).forGetter(AnimationKeyframe::getInterpolation)
            ).apply(instance, AnimationKeyframe::new)
    );

    protected final InterpolatedVector3f scale;
    protected final InterpolatedVector3f position;
    protected final InterpolatedVector3f rotation;
    protected final InterpolatedFloat alpha;
    protected final Interpolation interpolation;
    protected final int duration;

    protected int ticks;

    /**
     * @param duration The duration of the keyframe in ticks.
     * @param alpha The target alpha value of the keyframe.
     * @param position The offset position to reach at the end of the keyframe.
     * @param rotation The rotation to reach at the end of the keyframe.
     * @param scales The scale to reach at the end of the keyframe.
     * @param interpolation Interpolation type to use for the keyframe.
     */
    public AnimationKeyframe(int duration, float alpha, Vector3f position, Vector3f rotation, Vector3f scales, Interpolation interpolation) {
        if (duration < 0 || alpha < 0) {
            throw new IllegalArgumentException("Invalid keyframe parameters: " + duration + ", " + alpha + ", " + scales + ", " + interpolation);
        }

        this.duration = duration;
        this.alpha = new InterpolatedFloat(0, alpha);
        this.scale = new InterpolatedVector3f(new Vector3f(1, 1, 1), scales);
        this.position = new InterpolatedVector3f(new Vector3f(0, 0, 0), position);
        this.rotation = new InterpolatedVector3f(new Vector3f(0, 0, 0), rotation);

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
        this.alpha.setStart(sAlpha);
    }

    public void setStartingScale(Vector3f sScale) {
        this.scale.setStart(sScale);
    }

    public void setStartingPosition(Vector3f sScale) {
        this.position.setStart(sScale);
    }

    public void setStartingRotation(Vector3f sScale) {
        this.rotation.setStart(sScale);
    }

    @Override
    public void dispose() {
        this.ticks = 0;
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
        return this.alpha.interpolate(this.getProgress(), this.interpolation);
    }

    public Vector3f getScale() {
        return this.scale.interpolate(this.getProgress(), this.interpolation);
    }

    public Vector3f getPosition() {
        return this.position.interpolate(this.getProgress(), this.interpolation);
    }

    public Vector3f getRotation() {
        return this.rotation.interpolate(this.getProgress(), this.interpolation);
    }

    public float getProgress() {
        return (float) this.ticks / this.duration;
    }

    private int getDuration() {
        return this.duration;
    }

    private float getTargetAlpha() {
        return this.alpha.target;
    }

    private Interpolation getInterpolation() {
        return this.interpolation;
    }

    private Vector3f getTargetScale() {
        return this.scale.target;
    }

    private Vector3f getTargetPosition() {
        return this.position.target;
    }

    private Vector3f getTargetRotation() {
        return this.rotation.target;
    }

    public AnimationKeyframe instantiate() {
        AnimationKeyframe created = new AnimationKeyframe(this.duration, this.getTargetAlpha(), this.getTargetPosition(), this.getTargetRotation(), this.getTargetScale(), this.interpolation);

        created.setStartingScale(this.scale.start());
        created.setStartingAlpha(this.alpha.start());
        created.setStartingPosition(this.position.start());

        return created;
    }

    public interface InterpolatedValue<T> {
        T start();
        T target();
        T interpolate(float progress, Interpolation type);

        void setStart(T start);
    }

    public static class InterpolatedFloat implements InterpolatedValue<Float> {
        private float start;
        private final float target;

        public InterpolatedFloat(float start, float target) {
            this.start = start;
            this.target = target;
        }

        @Override
        public Float start() {
            return this.start;
        }

        @Override
        public Float target() {
            return this.target;
        }

        @Override
        public Float interpolate(float progress, Interpolation type) {
            return type.interpolate(progress, this.start(), this.target());
        }

        @Override
        public void setStart(Float start) {
            this.start = start;
        }
    }

    public static class InterpolatedVector3f implements InterpolatedValue<Vector3f> {
        private Vector3f start;
        private final Vector3f target;

        public InterpolatedVector3f(Vector3f start, Vector3f target) {
            this.start = start;
            this.target = target;
        }

        @Override
        public Vector3f start() {
            return this.start;
        }

        @Override
        public Vector3f target() {
            return this.target;
        }

        @Override
        public Vector3f interpolate(float progress, Interpolation type) {
            return new Vector3f(
                type.interpolate(progress, this.start.x, this.target.x),
                type.interpolate(progress, this.start.y, this.target.y),
                type.interpolate(progress, this.start.z, this.target.z)
            );
        }

        @Override
        public void setStart(Vector3f start) {
            this.start = start;
        }
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
