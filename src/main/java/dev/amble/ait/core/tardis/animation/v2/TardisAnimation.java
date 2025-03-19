package dev.amble.ait.core.tardis.animation.v2;

import dev.amble.ait.api.tardis.Disposable;
import dev.amble.ait.api.tardis.TardisTickable;
import dev.amble.ait.core.tardis.animation.v2.keyframe.KeyframeTracker;
import dev.amble.lib.api.Identifiable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

/**
 * Represents an exterior animation for the TARDIS.
 */
public abstract class TardisAnimation implements TardisTickable, Disposable, Identifiable {
	private final Identifier id;
	protected final KeyframeTracker tracker;

	protected TardisAnimation(Identifier id, KeyframeTracker tracker) {
		this.id = id;
		this.tracker = tracker;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void tick(MinecraftClient client) {
		this.tracker.tick(client);
	}

	@Override
	public void tick(MinecraftServer server) {
		this.tracker.tick(server);
	}

	@Override
	public void dispose() {
		this.tracker.dispose();
	}

	@Override
	public boolean isAged() {
		return this.tracker.isAged();
	}

	@Override
	public void age() {
		this.tracker.age();
	}

	@Override
	public Identifier id() {
		return this.id;
	}

	public float getAlpha() {
		return this.tracker.getAlpha();
	}

	// required for datapacks
	public KeyframeTracker tracker() {
		return this.tracker;
	}
}
