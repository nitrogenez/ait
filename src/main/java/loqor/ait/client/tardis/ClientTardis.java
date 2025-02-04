package loqor.ait.client.tardis;

import java.lang.reflect.Type;
import java.util.UUID;

import com.google.gson.InstanceCreator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import loqor.ait.AITMod;
import loqor.ait.api.Disposable;
import loqor.ait.api.TardisComponent;
import loqor.ait.client.util.ClientShakeUtil;
import loqor.ait.client.util.ClientTardisUtil;
import loqor.ait.core.tardis.Tardis;
import loqor.ait.core.tardis.TardisDesktop;
import loqor.ait.core.tardis.TardisExterior;
import loqor.ait.core.tardis.dim.TardisDimension;
import loqor.ait.data.Exclude;

public class ClientTardis extends Tardis implements Disposable {

    @Exclude
    private final UUID check;

    @Exclude
    private boolean aged = false;

    @Exclude
    public int ticks = 0;

    private ClientTardis(UUID check) {
        super();
        this.check = check;
    }

    public void setDesktop(TardisDesktop desktop) {
        desktop.setTardis(this);
        this.desktop = desktop;
    }

    public void setExterior(TardisExterior exterior) {
        exterior.setTardis(this);
        this.exterior = exterior;
    }

    public void tick(MinecraftClient client) {
        // referencing client stuff where it COULD be server causes problems
        if (ClientShakeUtil.shouldShake(this)) {
            if (this.flight().falling().get()) {
                ClientShakeUtil.ShakeFromEverywhere();
            }
            ClientShakeUtil.shakeFromConsole();
        }

        if (this.equals(ClientTardisUtil.getCurrentTardis())) {
            ClientTardisUtil.tickPowerDelta();
            ClientTardisUtil.tickAlarmDelta();
            ticks++;
        }
    }

    @Override
    public <T extends TardisComponent> T handler(TardisComponent.IdLike type) {
        if (this.handlers == null) {
            AITMod.LOGGER.error("Asked for a handler too early on {}", this);
            return null;
        }

        return super.handler(type);
    }

    public void age() {
        this.aged = true;
    }

    @Override
    public boolean isAged() {
        return aged;
    }

    @Override
    public void dispose() {
        this.desktop.dispose();
        this.desktop = null;

        this.exterior.dispose();
        this.exterior = null;

        this.handlers.dispose();
        this.handlers = null;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + Integer.toHexString(check.hashCode()) + ")";
    }

    @Override
    public ClientWorld getInteriorWorld() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return null;
        if (!(TardisDimension.isTardisDimension(world))) return null;

        return world;
    }

    public static Object creator() {
        return new ClientTardisCreator();
    }

    static class ClientTardisCreator implements InstanceCreator<ClientTardis> {

        @Override
        public ClientTardis createInstance(Type type) {
            return new ClientTardis(UUID.randomUUID());
        }
    }
}
