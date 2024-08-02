package loqor.ait.tardis.exterior.variant.easter_head.client;

import loqor.ait.AITMod;
import loqor.ait.client.models.exteriors.EasterHeadModel;
import loqor.ait.client.models.exteriors.ExteriorModel;
import loqor.ait.core.data.schema.exterior.ClientExteriorVariantSchema;
import loqor.ait.tardis.data.BiomeHandler;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

// a useful class for creating easter_head variants as they all have the same filepath you know
public abstract class ClientEasterHeadVariant extends ClientExteriorVariantSchema {
	private final String name;
	protected static final String CATEGORY_PATH = "textures/blockentities/exteriors/easter_head";
	protected static final Identifier CATEGORY_IDENTIFIER = new Identifier(AITMod.MOD_ID, CATEGORY_PATH + "/easter_head.png");
	protected static final String TEXTURE_PATH = CATEGORY_PATH + "/easter_head_";

	protected ClientEasterHeadVariant(String name) {
		super(new Identifier(AITMod.MOD_ID, "exterior/easter_head/" + name));

		this.name = name;
	}


	@Override
	public ExteriorModel model() {
		return new EasterHeadModel(EasterHeadModel.getTexturedModelData().createModel());
	}

	@Override
	public Identifier texture() {
		return new Identifier(AITMod.MOD_ID, TEXTURE_PATH + name + ".png");
	}

	@Override
	public Identifier emission() {
		return null;
	}

	@Override
	public Vector3f sonicItemTranslations() {
		return new Vector3f(0.25f, 1.1f, 1.2f);
	}

	@Override
	public float[] sonicItemRotations() {
		return new float[]{0f, 112.5f};
	}

	@Override
	public Identifier getBiomeTexture(BiomeHandler.BiomeType biomeType) {
		return switch(biomeType) {
			case DEFAULT -> null;
			case SNOWY -> BiomeHandler.BiomeType.SNOWY.getTexture(CATEGORY_IDENTIFIER);
			case SCULK -> BiomeHandler.BiomeType.SCULK.getTexture(CATEGORY_IDENTIFIER);
			case SANDY -> BiomeHandler.BiomeType.SANDY.getTexture(CATEGORY_IDENTIFIER);
			case RED_SANDY -> BiomeHandler.BiomeType.RED_SANDY.getTexture(CATEGORY_IDENTIFIER);
			case MUDDY -> BiomeHandler.BiomeType.MUDDY.getTexture(CATEGORY_IDENTIFIER);
			case CHORUS -> BiomeHandler.BiomeType.CHORUS.getTexture(CATEGORY_IDENTIFIER);
			case CHERRY -> BiomeHandler.BiomeType.CHERRY.getTexture(CATEGORY_IDENTIFIER);
		};
	}
}