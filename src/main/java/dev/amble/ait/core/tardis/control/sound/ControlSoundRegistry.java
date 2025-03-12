package dev.amble.ait.core.tardis.control.sound;

import dev.amble.lib.register.datapack.SimpleDatapackRegistry;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import dev.amble.ait.AITMod;
import dev.amble.ait.core.AITSounds;
import dev.amble.ait.core.tardis.control.Control;
import dev.amble.ait.core.tardis.control.impl.*;
import dev.amble.ait.data.schema.console.ConsoleTypeSchema;

public class ControlSoundRegistry extends SimpleDatapackRegistry<ControlSound> {
    private static final ControlSoundRegistry instance = new ControlSoundRegistry();

    public ControlSoundRegistry() {
        super(ControlSound::fromInputStream, ControlSound.CODEC, "control_sounds", true, AITMod.MOD_ID);
    }

    public static ControlSoundRegistry getInstance() {
        return instance;
    }

    public static ControlSound EMPTY;

    @Override
    public ControlSound fallback() {
        return EMPTY;
    }

    @Override
    protected void defaults() {
        EMPTY = new ControlSound(AITMod.id("empty"), AITMod.id("empty"), AITSounds.ERROR.getId(), AITSounds.ERROR.getId());

        this.register(ControlSound.forFallback(AutoPilotControl.ID, AITSounds.PROTOCOL_116_ON, AITSounds.PROTOCOL_116_OFF));
        this.register(ControlSound.forFallback(CloakControl.ID, AITSounds.PROTOCOL_3, AITSounds.PROTOCOL_3ALT));
        this.register(ControlSound.forFallback(DoorControl.ID, AITSounds.DOOR_CONTROL, AITSounds.DOOR_CONTROLALT));
        this.register(ControlSound.forFallback(HandBrakeControl.ID, AITSounds.HANDBRAKE_UP, AITSounds.HANDBRAKE_DOWN));
        this.register(ControlSound.forFallback(RefuelerControl.ID, AITSounds.ENGINE_REFUEL_CRANK, AITSounds.ENGINE_REFUEL));
        this.register(ControlSound.forFallback(ShieldsControl.ID, AITSounds.HANDBRAKE_LEVER_PULL, AITSounds.SHIELDS));
    }

    /**
     * Get the control sound for a control and console
     * Will return
     * - the sound for the specific control and console
     * OR
     * - the fallback sound for the specific console as defined & registered using {@linkplain ControlSound#forFallback(Identifier, SoundEvent, SoundEvent)}
     * OR
     * - the fallback sound for the specific control as defined from {@linkplain Control#getFallbackSound()}
     * OR
     * - meow, meow meow meow.
     * @param consoleId console id to look for
     * @param controlId control id to look for
     * @return the control sound
     */
    public ControlSound get(Identifier consoleId, Identifier controlId) {
        ControlSound possible = this.get(ControlSound.mergeIdentifiers(controlId, consoleId));
        if (possible != null) {
            return possible;
        }

        System.out.println("ControlSoundRegistry.get: " + controlId + " " + consoleId);

        // iterate through to find matching
        for (ControlSound sound : this.REGISTRY.values()) {
            if (sound.controlId().equals(controlId) && sound.consoleId().equals(consoleId)) {
                return sound;
            }
        }

        // double iteration - wow
        for (ControlSound sound : this.REGISTRY.values()) {
            if (sound.controlId().equals(controlId) && sound.consoleId().equals(AITMod.id("fallback"))) {
                return sound;
            }
        }

        return this.fallback();
    }
    public ControlSound get(ConsoleTypeSchema console, Control control) {
        return this.get(console.id(), control.id());
    }
}
