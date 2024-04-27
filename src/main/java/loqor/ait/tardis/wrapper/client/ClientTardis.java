package loqor.ait.tardis.wrapper.client;

import loqor.ait.client.registry.exterior.ClientExteriorVariantSchema;
import loqor.ait.client.util.ClientShakeUtil;
import loqor.ait.client.util.ClientTardisUtil;
import loqor.ait.tardis.*;
import loqor.ait.tardis.data.SonicHandler;
import loqor.ait.tardis.data.TardisHandlersManager;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import loqor.ait.tardis.exterior.category.ExteriorCategorySchema;
import loqor.ait.tardis.exterior.variant.ExteriorVariantSchema;
import loqor.ait.tardis.data.DoorData;
import loqor.ait.tardis.util.AbsoluteBlockPos;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

// Things saved here will likely get overwritten.
public class ClientTardis extends Tardis {
	public ClientTardis(UUID uuid, AbsoluteBlockPos.Client pos, TardisDesktopSchema schema, ExteriorCategorySchema exteriorType, ExteriorVariantSchema variantType, boolean locked) {
		super(uuid, tardis -> new TardisTravel(tardis, pos), tardis -> new TardisDesktop(tardis, schema), tardis -> new ClientTardisExterior(tardis, exteriorType, variantType));
	}

	public void setDesktop(TardisDesktop desktop) {
		this.desktop = desktop;
	}

	public void setTravel(TardisTravel travel) {
		this.travel = travel;
	}

	public void setSonic(SonicHandler sonicHandler) {
		this.handlers.setSonic(sonicHandler);
	}

	public void setExterior(TardisExterior exterior) {
		this.exterior = exterior;
	}

	public void setDoor(DoorData door) {
		this.getHandlers().setDoor(door);
	}

	public void setHandlers(TardisHandlersManager handlers) {
		this.handlers = handlers;
	}

	public boolean isExteriorUnlocked(ClientExteriorVariantSchema schema) {
		return PropertiesHandler.isExteriorUnlocked(getHandlers().getProperties(), schema);
	}

	public void tick(MinecraftClient client) {
		// referencing client stuff where it COULD be server causes problems
		if (ClientShakeUtil.shouldShake(this)) {
			ClientShakeUtil.shakeFromConsole();
		}

		if (this.equals(ClientTardisUtil.getCurrentTardis())) {
			ClientTardisUtil.tickPowerDelta();
			ClientTardisUtil.tickAlarmDelta();
		}
	}
}