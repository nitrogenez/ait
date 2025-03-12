package dev.amble.ait.core.tardis.control.sound;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.amble.ait.core.AITSounds;
import dev.amble.lib.api.Identifiable;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import dev.amble.ait.AITMod;
import dev.amble.ait.core.tardis.control.Control;
import dev.amble.ait.data.schema.console.ConsoleTypeSchema;
import dev.amble.ait.registry.impl.ControlRegistry;
import dev.amble.ait.registry.impl.console.ConsoleRegistry;

/**
 * Represents a sound that is played when a control is used
 * @param id The identifier of this sound - it's recommended to make this null in the constructor. It will be automatically generated
 * @param controlId The identifier of the control that this sound is for
 * @param consoleId The identifier of the console that this sound is for
 * @param successSound The sound that is played when the control is successful
 * @param altSound The sound that is played when the control fails OR a value is switched
 * @see Control
 * @see ConsoleTypeSchema
 * @author duzo
 */
public record ControlSound(Identifier id, Identifier controlId, Identifier consoleId, SoundEvent successSound, SoundEvent altSound) implements Identifiable {
    public static final Codec<ControlSound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("id", null).forGetter(ControlSound::id),
            Identifier.CODEC.fieldOf("control_id").forGetter(ControlSound::controlId),
            Identifier.CODEC.fieldOf("console_id").forGetter(ControlSound::consoleId),
            SoundEvent.CODEC.fieldOf("success_sound").forGetter(ControlSound::successSound),
            SoundEvent.CODEC.optionalFieldOf("alt_sound", AITSounds.ERROR).forGetter(ControlSound::altSound)
    ).apply(instance, ControlSound::new));

    public ControlSound {
        if (id == null) {
            id = mergeIdentifiers(controlId, consoleId);
        }
    }

    public ConsoleTypeSchema console() {
        return ConsoleRegistry.REGISTRY.get(this.consoleId());
    }
    public Control control() {
        return ControlRegistry.REGISTRY.get(this.controlId());
    }
    public SoundEvent sound(Control.Result result) {
        return !result.isAltSound() ? this.successSound() : this.altSound();
    }

	public static ControlSound forFallback(Identifier controlId, SoundEvent success, SoundEvent alt) {
		return new ControlSound(null, controlId, AITMod.id("fallback"), success, alt);
	}

    public static Identifier mergeIdentifiers(Identifier controlId, Identifier consoleId) {
        return new Identifier(controlId.getNamespace(), controlId.getPath() + "_" + consoleId.getPath());
    }

    public static ControlSound fromInputStream(InputStream stream) {
        return fromJson(JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject());
    }
    public static ControlSound fromJson(JsonObject json) {
        AtomicReference<ControlSound> created = new AtomicReference<>();

        CODEC.decode(JsonOps.INSTANCE, json).get().ifLeft(var -> created.set(var.getFirst())).ifRight(err -> {
            created.set(null);
            AITMod.LOGGER.error("Error decoding datapack console variant: {}", err);
        });

        return created.get();
    }
}
