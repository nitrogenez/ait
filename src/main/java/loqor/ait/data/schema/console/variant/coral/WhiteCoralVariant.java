package loqor.ait.data.schema.console.variant.coral;

import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.core.tardis.handler.loyalty.Loyalty;
import loqor.ait.data.schema.console.ConsoleVariantSchema;
import loqor.ait.data.schema.console.type.CoralType;

public class WhiteCoralVariant extends ConsoleVariantSchema {
    public static final Identifier REFERENCE = new Identifier(AITMod.MOD_ID, "console/coral_white");

    public WhiteCoralVariant() {
        super(CoralType.REFERENCE, REFERENCE, new Loyalty(Loyalty.Type.PILOT));
    }
}