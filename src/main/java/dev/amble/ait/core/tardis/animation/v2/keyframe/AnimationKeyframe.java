package dev.amble.ait.core.tardis.animation.v2.keyframe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.joml.Vector3f;

import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;

import dev.amble.ait.api.tardis.Disposable;
import dev.amble.ait.api.tardis.TardisTickable;

/**
 * Represents a keyframe in an animation.
 * The keyframe interpolates between two alpha values over a duration of ticks.
 */
public class AnimationKeyframe<T> implements TardisTickable, Disposable {
    protected final InterpolatedValue<T> value;
    protected final Interpolation interpolation;
    protected final float duration;
    protected int ticks;

    public AnimationKeyframe(float duration, Interpolation type, InterpolatedValue<T> value) {
        if (duration < 0 || value == null) {
            throw new IllegalArgumentException("Invalid keyframe parameters: " + duration + ", " + type + ", " + value);
        }

        this.duration = duration;
        this.value = value;

        this.interpolation = type;

        this.ticks = 0;
    }

    public boolean isDone() {
        return ticks >= duration;
    }

    public void tickCommon(boolean isClient) {
        ticks++;
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
        this.ticks = MathHelper.ceil(this.duration);
    }

    public int ticks() {
        return this.ticks;
    }

    /**
     * Get the current value of the keyframe.
     * @param delta Delta time between ticks in rendering
     * @return The current value of the keyframe.
     */
    public T getValue(float delta) {
        return this.value.interpolate(this.getProgress(delta), this.interpolation);
    }

    public void setStart(T val) {
        this.value.setStart(val);
    }

    /**
     * Get the current progress of the keyframe.
     * @param delta Delta time between ticks in rendering
     * @return
     */
    public float getProgress(float delta) {
        return (this.ticks + delta) / this.duration;
    }

    public AnimationKeyframe<T> instantiate() {
        return new AnimationKeyframe<>(this.duration, this.interpolation, this.value.instantiate());
    }

    public interface InterpolatedValue<T> {
        T start();
        T target();
        T interpolate(float progress, Interpolation type);

        void setStart(T start);
        InterpolatedValue<T> instantiate();
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

        @Override
        public InterpolatedValue<Float> instantiate() {
            return new InterpolatedFloat(this.start, this.target);
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
            synchronized (this) {
                this.start = start;
            }
        }

        @Override
        public InterpolatedValue<Vector3f> instantiate() {
            return new InterpolatedVector3f(new Vector3f(this.start), new Vector3f(this.target));
        }
    }

    public enum Interpolation {
        LINEAR {
            @Override
            public float interpolate(float progress, float start, float end) {
                return MathHelper.lerp(Math.min(progress, 1), start, end);
            }
        },
        CUBIC {;
            @Override
            public float interpolate(float progress, float start, float end) {
                return MathHelper.catmullRom(Math.min(progress, 1), start, start, end, end);
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
