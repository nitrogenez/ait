package dev.amble.ait.core.tardis.control.sound;

import dev.amble.lib.register.datapack.SimpleDatapackRegistry;

import net.minecraft.util.Identifier;

import dev.amble.ait.AITMod;
import dev.amble.ait.core.AITSounds;
import dev.amble.ait.core.tardis.control.Control;
import dev.amble.ait.data.schema.console.ConsoleTypeSchema;

public class ControlSoundRegistry extends SimpleDatapackRegistry<ControlSound> {
    private static final ControlSoundRegistry instance = new ControlSoundRegistry();

    public ControlSoundRegistry() {
        super(ControlSound::fromInputStream, ControlSound.CODEC, "console_sounds", true, AITMod.MOD_ID);
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
        EMPTY = new ControlSound(null, AITMod.id("empty"), AITMod.id("empty"), AITSounds.ERROR, AITSounds.ERROR);
    }

    /**
     * Get the control sound for a control and console
     * Will return
     * - the sound for the specific control and console
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

        // iterate through to find matching
        for (ControlSound sound : this.REGISTRY.values()) {
            if (sound.controlId().equals(controlId) && sound.consoleId().equals(consoleId)) {
                return sound;
            }
        }

        return this.fallback();
    }
    public ControlSound get(ConsoleTypeSchema console, Control control) {
        return this.get(console.id(), control.id());
    }
}
