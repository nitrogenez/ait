package mdteam.ait.tardis.handler;

import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.util.SerialDimension;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisTickable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.UUID;

public abstract class TardisLink implements TardisTickable {
    protected final UUID tardisId;

    public TardisLink(UUID tardisId) {
        this.tardisId = tardisId;
    }

    public void sync() {
        if (isClient()) return;

        ServerTardisManager.getInstance().sendToSubscribers(this.tardis());
    }

    public Tardis tardis() {
        if (isClient()) {
            return ClientTardisManager.getInstance().getLookup().get(tardisId);
        }

        return ServerTardisManager.getInstance().getTardis(tardisId);
    }

    @Override
    public void tick(ServerWorld world) {
    }

    @Override
    public void tick(MinecraftServer server) {
    }

    public AbsoluteBlockPos.Directed getDoorPos() {
        Tardis tardis = tardis();
        if (tardis == null || tardis.getDesktop() == null)
            return new AbsoluteBlockPos.Directed(0, 0, 0, new SerialDimension(World.OVERWORLD.getValue().toString()), Direction.NORTH);
        ;
        return tardis.getDesktop().getInteriorDoorPos();
    }

    public AbsoluteBlockPos.Directed getExteriorPos() {
        Tardis tardis = tardis();
        if (tardis == null || tardis.getTravel() == null)
            return new AbsoluteBlockPos.Directed(0, 0, 0, new SerialDimension(World.OVERWORLD.getValue().toString()), Direction.NORTH);
        return tardis.getTravel().getPosition();
    }

    public static boolean isClient() {
        return TardisUtil.isClient();
    }

    public static boolean isServer() {
        return TardisUtil.isServer();
    }
}