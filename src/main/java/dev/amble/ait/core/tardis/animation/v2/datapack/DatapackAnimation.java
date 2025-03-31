package dev.amble.ait.core.tardis.animation.v2.datapack;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

import dev.amble.ait.AITMod;
import dev.amble.ait.core.tardis.animation.v2.TardisAnimation;
import dev.amble.ait.core.tardis.animation.v2.keyframe.KeyframeTracker;
import dev.amble.ait.core.tardis.handler.travel.TravelHandlerBase;

public class DatapackAnimation extends TardisAnimation {
    public static final Codec<TardisAnimation> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Identifier.CODEC.fieldOf("id").forGetter(TardisAnimation::id),
                    KeyframeTracker.CODEC.fieldOf("tracker").forGetter(TardisAnimation::tracker),
                    TravelHandlerBase.State.CODEC.fieldOf("expected_state").forGetter(TardisAnimation::getExpectedState),
                    Codec.STRING.optionalFieldOf("name", "").forGetter(TardisAnimation::name)
    ).apply(instance, DatapackAnimation::new));

    private final TravelHandlerBase.State expectedState;
    private final String name;

    protected DatapackAnimation(Identifier id, KeyframeTracker tracker, TravelHandlerBase.State expectedState, String optName) {
        super(id, tracker);

        this.expectedState = expectedState;

        if (optName.isBlank()) {
            optName = id.getPath();
        }

        this.name = optName;
    }

    @Override
    public TravelHandlerBase.State getExpectedState() {
        return this.expectedState;
    }

    @Override
    public DatapackAnimation instantiate() {
        return new DatapackAnimation(this.id(), this.tracker.instantiate(), this.expectedState, this.name);
    }

    public static DatapackAnimation fromInputStream(InputStream stream) {
        return fromJson(JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject());
    }

    public static DatapackAnimation fromJson(JsonObject json) {
        AtomicReference<DatapackAnimation> created = new AtomicReference<>();

        CODEC.decode(JsonOps.INSTANCE, json).get().ifLeft(var -> {
            created.set((DatapackAnimation) var.getFirst());
        }).ifRight(err -> {
            created.set(null);
            AITMod.LOGGER.error("Error decoding datapack animation: {}", err);
        });

        return created.get();
    }

    @Override
    public String name() {
        return name;
    }
}
