package loqor.ait.registry.impl.exterior;

import dev.pavatus.register.datapack.DatapackRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import org.joml.Vector3f;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.client.models.exteriors.ExteriorModel;
import loqor.ait.data.datapack.DatapackExterior;
import loqor.ait.data.datapack.exterior.BiomeOverrides;
import loqor.ait.data.schema.exterior.ClientExteriorVariantSchema;
import loqor.ait.data.schema.exterior.ExteriorVariantSchema;
import loqor.ait.data.schema.exterior.variant.adaptive.client.ClientAdaptiveVariant;
import loqor.ait.data.schema.exterior.variant.bookshelf.client.ClientBookshelfDefaultVariant;
import loqor.ait.data.schema.exterior.variant.booth.client.*;
import loqor.ait.data.schema.exterior.variant.box.client.*;
import loqor.ait.data.schema.exterior.variant.capsule.client.ClientCapsuleDefaultVariant;
import loqor.ait.data.schema.exterior.variant.capsule.client.ClientCapsuleFireVariant;
import loqor.ait.data.schema.exterior.variant.capsule.client.ClientCapsuleSoulVariant;
import loqor.ait.data.schema.exterior.variant.classic.client.*;
import loqor.ait.data.schema.exterior.variant.doom.client.ClientDoomVariant;
import loqor.ait.data.schema.exterior.variant.easter_head.client.ClientEasterHeadDefaultVariant;
import loqor.ait.data.schema.exterior.variant.easter_head.client.ClientEasterHeadFireVariant;
import loqor.ait.data.schema.exterior.variant.easter_head.client.ClientEasterHeadSoulVariant;
import loqor.ait.data.schema.exterior.variant.geometric.client.ClientGeometricDefaultVariant;
import loqor.ait.data.schema.exterior.variant.geometric.client.ClientGeometricFireVariant;
import loqor.ait.data.schema.exterior.variant.geometric.client.ClientGeometricGildedVariant;
import loqor.ait.data.schema.exterior.variant.geometric.client.ClientGeometricSoulVariant;
import loqor.ait.data.schema.exterior.variant.growth.client.ClientGrowthVariant;
import loqor.ait.data.schema.exterior.variant.plinth.client.ClientPlinthDefaultVariant;
import loqor.ait.data.schema.exterior.variant.plinth.client.ClientPlinthFireVariant;
import loqor.ait.data.schema.exterior.variant.plinth.client.ClientPlinthSoulVariant;
import loqor.ait.data.schema.exterior.variant.renegade.client.ClientRenegadeCabinetVariant;
import loqor.ait.data.schema.exterior.variant.renegade.client.ClientRenegadeDefaultVariant;
import loqor.ait.data.schema.exterior.variant.renegade.client.ClientRenegadeTronVariant;
import loqor.ait.data.schema.exterior.variant.stallion.client.ClientStallionDefaultVariant;
import loqor.ait.data.schema.exterior.variant.stallion.client.ClientStallionFireVariant;
import loqor.ait.data.schema.exterior.variant.stallion.client.ClientStallionSoulVariant;
import loqor.ait.data.schema.exterior.variant.stallion.client.ClientStallionSteelVariant;
import loqor.ait.data.schema.exterior.variant.tardim.client.ClientTardimDefaultVariant;
import loqor.ait.data.schema.exterior.variant.tardim.client.ClientTardimFireVariant;
import loqor.ait.data.schema.exterior.variant.tardim.client.ClientTardimSoulVariant;

public class ClientExteriorVariantRegistry extends DatapackRegistry<ClientExteriorVariantSchema> implements
        SimpleSynchronousResourceReloadListener {

    private static final ClientExteriorVariantRegistry INSTANCE = new ClientExteriorVariantRegistry();

    public static DatapackRegistry<ClientExteriorVariantSchema> getInstance() {
        return INSTANCE;
    }

    /**
     * Will return the clients version of a servers door variant
     *
     * @return the first variant found as there should only be one client version
     */
    public static ClientExteriorVariantSchema withParent(ExteriorVariantSchema parent) {
        for (ClientExteriorVariantSchema schema : ClientExteriorVariantRegistry.getInstance().toList()) {
            if (schema.parent() == null)
                continue;

            if (schema.parent().id().equals(parent.id()))
                return schema;
        }

        return null;
    }

    @Override
    public ClientExteriorVariantSchema fallback() {
        return null;
    }

    /**
     * Do not call
     */
    @Override
    public void syncToClient(ServerPlayerEntity player) { }

    @Override
    public void readFromServer(PacketByteBuf buf) {
        int size = buf.readInt();

        for (int i = 0; i < size; i++) {
            this.register(convertDatapack(buf.decodeAsJson(DatapackExterior.CODEC)));
        }

        AITMod.LOGGER.info("Read {} exterior variants from server", size);
    }

    public static ClientExteriorVariantSchema convertDatapack(DatapackExterior variant) {
        if (!variant.wasDatapack())
            return convertNonDatapack(variant);

        return new ClientExteriorVariantSchema(variant.id()) {

            @Override
            public Identifier texture() {
                return variant.texture();
            }

            @Override
            public Identifier emission() {
                return variant.emission();
            }

            @Override
            public ExteriorModel model() {
                return getInstance().get(variant.getParentId()).model();
            }

            @Override
            public Vector3f sonicItemTranslations() {
                return new Vector3f(0.5f, 1.2f, 1.2f);
            }

            @Override
            public BiomeOverrides overrides() {
                return variant.overrides();
            }
        };
    }

    private static ClientExteriorVariantSchema convertNonDatapack(DatapackExterior variant) {
        if (variant.wasDatapack())
            return convertDatapack(variant);

        return getInstance().get(variant.id());
    }

    public static ClientExteriorVariantSchema TARDIM_DEFAULT;
    public static ClientExteriorVariantSchema TARDIM_FIRE;
    public static ClientExteriorVariantSchema TARDIM_SOUL;
    public static ClientExteriorVariantSchema BOX_DEFAULT;
    public static ClientExteriorVariantSchema BOX_FIRE;
    public static ClientExteriorVariantSchema BOX_SOUL;
    public static ClientExteriorVariantSchema BOX_FUTURE;
    public static ClientExteriorVariantSchema BOX_CORAL;
    public static ClientExteriorVariantSchema BOX_CHERRY;
    public static ClientExteriorVariantSchema BOX_TOKAMAK;
    public static ClientExteriorVariantSchema PRIME;
    public static ClientExteriorVariantSchema YETI;
    public static ClientExteriorVariantSchema DEFINITIVE;
    public static ClientExteriorVariantSchema PTORED;
    public static ClientExteriorVariantSchema MINT;
    public static ClientExteriorVariantSchema HUDOLIN;
    public static ClientExteriorVariantSchema SHALKA;
    public static ClientExteriorVariantSchema EXILE;
    public static ClientExteriorVariantSchema CAPSULE_DEFAULT;
    public static ClientExteriorVariantSchema CAPSULE_SOUL;
    public static ClientExteriorVariantSchema CAPSULE_FIRE;
    public static ClientExteriorVariantSchema BOOTH_DEFAULT;
    public static ClientExteriorVariantSchema BOOTH_FIRE;
    public static ClientExteriorVariantSchema BOOTH_SOUL;
    public static ClientExteriorVariantSchema BOOTH_VINTAGE;
    public static ClientExteriorVariantSchema BOOTH_BLUE;
    public static ClientExteriorVariantSchema BOOTH_GILDED;
    public static ClientExteriorVariantSchema HEAD_DEFAULT;
    public static ClientExteriorVariantSchema HEAD_SOUL;
    public static ClientExteriorVariantSchema HEAD_FIRE;
    public static ClientExteriorVariantSchema CORAL_GROWTH;
    public static ClientExteriorVariantSchema DOOM;
    public static ClientExteriorVariantSchema PLINTH_DEFAULT;
    public static ClientExteriorVariantSchema PLINTH_SOUL;
    public static ClientExteriorVariantSchema PLINTH_FIRE;
    public static ClientExteriorVariantSchema RENEGADE_DEFAULT;
    public static ClientExteriorVariantSchema RENEGADE_TRON;
    public static ClientExteriorVariantSchema RENEGADE_CABINET;
    public static ClientExteriorVariantSchema BOOKSHELF_DEFAULT;
    public static ClientExteriorVariantSchema GEOMETRIC_DEFAULT;
    public static ClientExteriorVariantSchema GEOMETRIC_FIRE;
    public static ClientExteriorVariantSchema GEOMETRIC_SOUL;
    public static ClientExteriorVariantSchema GEOMETRIC_GILDED;
    public static ClientExteriorVariantSchema STALLION_DEFAULT;
    public static ClientExteriorVariantSchema STALLION_FIRE;
    public static ClientExteriorVariantSchema STALLION_SOUL;
    public static ClientExteriorVariantSchema STALLION_STEEL;
    public static ClientExteriorVariantSchema ADAPTIVE;

    @Override
    public void onClientInit() {
        // TARDIM
        TARDIM_DEFAULT = register(new ClientTardimDefaultVariant());
        TARDIM_FIRE = register(new ClientTardimFireVariant());
        TARDIM_SOUL = register(new ClientTardimSoulVariant());

        // Police Box
        BOX_DEFAULT = register(new ClientPoliceBoxDefaultVariant());
        BOX_SOUL = register(new ClientPoliceBoxSoulVariant());
        BOX_FIRE = register(new ClientPoliceBoxFireVariant());
        BOX_FUTURE = register(new ClientPoliceBoxFuturisticVariant());
        BOX_CORAL = register(new ClientPoliceBoxCoralVariant());
        BOX_TOKAMAK = register(new ClientPoliceBoxTokamakVariant());
        BOX_CHERRY = register(new ClientPoliceBoxCherryVariant());

        // Classic Box
        PRIME = register(new ClientClassicBoxPrimeVariant());
        YETI = register(new ClientClassicBoxYetiVariant());
        DEFINITIVE = register(new ClientClassicBoxDefinitiveVariant());
        PTORED = register(new ClientClassicBoxPtoredVariant());
        MINT = register(new ClientClassicBoxMintVariant());
        HUDOLIN = register(new ClientClassicBoxHudolinVariant());
        SHALKA = register(new ClientClassicBoxShalkaVariant());
        EXILE = register(new ClientClassicBoxExileVariant());

        // Capsule
        CAPSULE_DEFAULT = register(new ClientCapsuleDefaultVariant());
        CAPSULE_SOUL = register(new ClientCapsuleSoulVariant());
        CAPSULE_FIRE = register(new ClientCapsuleFireVariant());

        // Booth
        BOOTH_DEFAULT = register(new ClientBoothDefaultVariant());
        BOOTH_FIRE = register(new ClientBoothFireVariant());
        BOOTH_SOUL = register(new ClientBoothSoulVariant());
        BOOTH_VINTAGE = register(new ClientBoothVintageVariant());
        BOOTH_BLUE = register(new ClientBoothBlueVariant());
        BOOTH_GILDED = register(new ClientBoothGildedVariant());

        // Easter Head
        HEAD_DEFAULT = register(new ClientEasterHeadDefaultVariant());
        HEAD_SOUL = register(new ClientEasterHeadSoulVariant());
        HEAD_FIRE = register(new ClientEasterHeadFireVariant());

        // Coral
        CORAL_GROWTH = register(new ClientGrowthVariant());

        // Doom
        DOOM = register(new ClientDoomVariant());

        // Plinth
        PLINTH_DEFAULT = register(new ClientPlinthDefaultVariant());
        PLINTH_SOUL = register(new ClientPlinthSoulVariant());
        PLINTH_FIRE = register(new ClientPlinthFireVariant());

        // Renegade
        RENEGADE_DEFAULT = register(new ClientRenegadeDefaultVariant());
        RENEGADE_TRON = register(new ClientRenegadeTronVariant());
        RENEGADE_CABINET = register(new ClientRenegadeCabinetVariant());

        // Bookshelf
        BOOKSHELF_DEFAULT = register(new ClientBookshelfDefaultVariant());

        // Geometric
        GEOMETRIC_DEFAULT = register(new ClientGeometricDefaultVariant());
        GEOMETRIC_FIRE = register(new ClientGeometricFireVariant());
        GEOMETRIC_SOUL = register(new ClientGeometricSoulVariant());
        GEOMETRIC_GILDED = register(new ClientGeometricGildedVariant());

        // Stallion
        STALLION_DEFAULT = register(new ClientStallionDefaultVariant());
        STALLION_FIRE = register(new ClientStallionFireVariant());
        STALLION_SOUL = register(new ClientStallionSoulVariant());
        STALLION_STEEL = register(new ClientStallionSteelVariant());

        ADAPTIVE = register(new ClientAdaptiveVariant());
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(AITMod.MOD_ID, "client_exterior");
    }

    @Override
    public void onCommonInit() {
        super.onCommonInit();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
    }

    @Override
    public void reload(ResourceManager manager) {
        for (ClientExteriorVariantSchema schema : REGISTRY.values()) {
            BiomeOverrides overrides = schema.overrides();

            if (overrides == null)
                continue;

            overrides.validate();
        }
    }
}
