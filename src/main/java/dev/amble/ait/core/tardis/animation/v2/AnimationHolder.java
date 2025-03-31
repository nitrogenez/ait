package dev.amble.ait.core.tardis.animation.v2;

import java.util.UUID;

import dev.amble.ait.core.tardis.animation.v2.datapack.TardisAnimationRegistry;
import dev.amble.ait.data.Exclude;
import dev.amble.lib.util.ServerLifecycleHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import dev.amble.ait.AITMod;
import dev.amble.ait.api.tardis.Disposable;
import dev.amble.ait.api.tardis.TardisTickable;
import dev.amble.ait.api.tardis.link.v2.Linkable;
import dev.amble.ait.api.tardis.link.v2.TardisRef;
import dev.amble.ait.core.tardis.ServerTardis;
import dev.amble.ait.core.tardis.Tardis;
import dev.amble.ait.core.tardis.TardisManager;
import dev.amble.ait.core.tardis.handler.travel.TravelHandlerBase;
import dev.amble.ait.core.tardis.util.NetworkUtil;

public class AnimationHolder implements TardisTickable, Disposable, Linkable {
    public static final Identifier UPDATE_PACKET = AITMod.id("sync/ext_anim");

    protected final TardisAnimationMap map;
    private TardisAnimation current;
    private float alphaOverride = -1;
    @Exclude
    private boolean isServer = true;
    private TardisRef ref;

    public AnimationHolder(TardisAnimationMap map) {
        this.map = map;
    }

    public AnimationHolder(Tardis tardis) {
        this(tardis.stats().getTravelAnimations());

        this.link(tardis);
    }

    protected TardisAnimation getCurrent() {
        return this.current;
    }

    @Override
    public void tick(MinecraftServer server) {
        if (this.getCurrent() == null) return;

        this.getCurrent().tick(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void tick(MinecraftClient client) {
        this.isServer = false;

        if (this.getCurrent() == null) return;

        this.getCurrent().tick(client);
    }

    @Override
    public boolean isAged() {
        return this.getCurrent().isAged();
    }

    @Override
    public void age() {
        this.getCurrent().age();
    }

    @Override
    public void dispose() {
        this.getCurrent().dispose();
        this.alphaOverride = -1;
    }

    @Override
    public void link(UUID uuid) {
        this.ref = new TardisRef(uuid, real -> TardisManager.with(!this.isServer, (o, manager) -> manager.demandTardis(o, real), ServerLifecycleHooks::get));
    }

    @Override
    public void link(Tardis tardis) {
        this.ref = new TardisRef(tardis, real -> TardisManager.with(!this.isServer, (o, manager) -> manager.demandTardis(o, real), ServerLifecycleHooks::get));

        this.isServer = tardis instanceof ServerTardis;
    }

    @Override
    public TardisRef tardis() {
        return this.ref;
    }

    public void onStateChange(TravelHandlerBase.State state) {
        TardisAnimation animation = this.map.get(state);

        if (state == TravelHandlerBase.State.LANDED) {
            this.alphaOverride = 1f;
            return;
        } else if (state == TravelHandlerBase.State.FLIGHT) {
            this.alphaOverride = 0f;
            return;
        }

        if (animation == null) {
            switch (state) {
                case DEMAT:
                    this.alphaOverride = 1f;
                case MAT:
                    this.alphaOverride = 0f;
            }
            return;
        }

        this.alphaOverride = -1;

        if (this.getCurrent() != null) {
            this.getCurrent().dispose();
        }

        animation.dispose();;
        this.current = animation.instantiate();

        if (this.isLinked()) {
            this.getCurrent().link(this.tardis().get());
        }

        this.sync(state);
    }

    public float getAlpha() {
        if (this.alphaOverride != -1) {
            return this.alphaOverride;
        }

/*        if (!this.isServer && ServerLifecycleHooks.get().getTicks() % 20 == 0) {
            System.out.println(this);
            System.out.println(this.getCurrent());
        }*/

        if (this.getCurrent() == null)
             return 1f;

        return this.getCurrent().getAlpha();
    }

    private void sync(TravelHandlerBase.State state) {
        if (!ServerLifecycleHooks.isServer() || !this.isLinked() || !(this.tardis().get() instanceof ServerTardis)) return;

        ServerTardis tardis = this.tardis().get().asServer();

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeEnumConstant(state);
        buf.writeUuid(tardis.getUuid());

        NetworkUtil.getSubscribedPlayers(tardis).forEach(player -> {;
            NetworkUtil.send(player, UPDATE_PACKET, buf);
        });
    }
}
