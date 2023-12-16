package mdteam.ait.tardis.wrapper.client;

import mdteam.ait.client.renderers.consoles.ConsoleEnum;
import mdteam.ait.client.renderers.exteriors.ExteriorEnum;
import mdteam.ait.client.renderers.exteriors.VariantEnum;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisDesktop;
import mdteam.ait.tardis.TardisDesktopSchema;
import mdteam.ait.tardis.TardisTravel;

import java.util.UUID;

public class ClientTardis extends Tardis {

    public ClientTardis(UUID uuid, AbsoluteBlockPos.Client pos, TardisDesktopSchema schema, ExteriorEnum exteriorType, VariantEnum variantType, ConsoleEnum consoleType, boolean locked) {
        super(uuid, tardis -> new TardisTravel(tardis, pos), tardis -> new TardisDesktop(tardis, schema), tardis -> new ClientTardisExterior(tardis, exteriorType, variantType), tardis -> new ClientTardisConsole(tardis, consoleType, consoleType.getControlTypesList()), locked);
    }

}