package loqor.ait.data.schema.console.variant.toyota;

import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.core.tardis.handler.loyalty.Loyalty;
import loqor.ait.data.schema.console.ConsoleVariantSchema;
import loqor.ait.data.schema.console.type.ToyotaType;

public class ToyotaBlueVariant extends ConsoleVariantSchema {
    public static final Identifier REFERENCE = new Identifier(AITMod.MOD_ID, "console/toyota_blue");

    public ToyotaBlueVariant() {
        super(ToyotaType.REFERENCE, REFERENCE, new Loyalty(Loyalty.Type.PILOT));
    }
}