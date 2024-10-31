package loqor.ait.core.sounds.travel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.api.Identifiable;
import loqor.ait.core.tardis.handler.travel.TravelHandlerBase;

// @TODO better variable names
public record TravelSound(TravelHandlerBase.State target, Identifier id, Identifier soundId, int timeLeft, int maxTime, int startTime, int length, float frequency,
                          float intensity) implements Identifiable {
    public static final Codec<TravelSound> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    TravelHandlerBase.State.CODEC.fieldOf("target").forGetter(TravelSound::target),
                    Identifier.CODEC.fieldOf("id").forGetter(TravelSound::id),
                    Identifier.CODEC.fieldOf("sound").forGetter(TravelSound::soundId),
                    Codec.INT.fieldOf("timeLeft").forGetter(TravelSound::timeLeft),
                    Codec.INT.fieldOf("maxTime").forGetter(TravelSound::maxTime),
                    Codec.INT.fieldOf("startTime").forGetter(TravelSound::startTime),
                    Codec.INT.fieldOf("length").forGetter(TravelSound::length),
                    Codec.FLOAT.fieldOf("frequency").forGetter(TravelSound::frequency),
                    Codec.FLOAT.fieldOf("intensity").forGetter(TravelSound::intensity))
            .apply(instance, TravelSound::new));

    @Override
    public Identifier id() {
        return this.id;
    }

    public SoundEvent sound() {
        return Registries.SOUND_EVENT.get(this.soundId());
    }

    public static TravelSound fromInputStream(InputStream stream) {
        return fromJson(JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject());
    }

    public static TravelSound fromJson(JsonObject json) {
        AtomicReference<TravelSound> created = new AtomicReference<>();

        CODEC.decode(JsonOps.INSTANCE, json).get().ifLeft(var -> created.set(var.getFirst())).ifRight(err -> {
            created.set(null);
            AITMod.LOGGER.error("Error decoding datapack travel sfx: {}", err);
        });

        return created.get();
    }
}
