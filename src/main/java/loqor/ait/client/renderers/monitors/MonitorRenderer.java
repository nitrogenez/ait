package loqor.ait.client.renderers.monitors;

import loqor.ait.AITMod;
import loqor.ait.client.models.monitors.CRTMonitorModel;
import loqor.ait.core.blockentities.MonitorBlockEntity;
import loqor.ait.core.data.AbsoluteBlockPos;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.TardisTravel;
import loqor.ait.tardis.control.impl.DimensionControl;
import loqor.ait.tardis.control.impl.DirectionControl;
import loqor.ait.tardis.data.FuelData;
import loqor.ait.tardis.util.FlightUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;

public class MonitorRenderer<T extends MonitorBlockEntity> implements BlockEntityRenderer<T> {

	public static final Identifier MONITOR_TEXTURE = new Identifier(AITMod.MOD_ID, ("textures/blockentities/monitors/crt_monitor.png"));
	public static final Identifier EMISSIVE_MONITOR_TEXTURE = new Identifier(AITMod.MOD_ID, ("textures/blockentities/monitors/crt_monitor_emission.png"));
	private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
	private final CRTMonitorModel crtMonitorModel;

	public MonitorRenderer(BlockEntityRendererFactory.Context ctx) {
		this.crtMonitorModel = new CRTMonitorModel(CRTMonitorModel.getTexturedModelData().createModel());
	}

	@Override
	public void render(MonitorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		BlockState blockState = entity.getCachedState();

		int k = blockState.get(SkullBlock.ROTATION);
		float h = RotationPropertyHelper.toDegrees(k);

		matrices.push();
		matrices.translate(0.5f, 1.5f, 0.5f);
		matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(h));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

		this.crtMonitorModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(MONITOR_TEXTURE)), light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
		this.crtMonitorModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(EMISSIVE_MONITOR_TEXTURE)), 0xF000F00, overlay, 1.0F, 1.0F, 1.0F, 1.0F);

		matrices.pop();

		if (entity.tardis().isEmpty())
			return;

		Tardis tardis = entity.tardis().get();

		if (!tardis.engine().hasPower())
			return;

		matrices.push();
		matrices.translate(0.5, 0.75, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f));
		matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(180 - h));
		matrices.scale(0.005f, 0.005f, 0.005f);
		matrices.translate(-50f, 0, -80);

		TardisTravel travel = tardis.travel();
		AbsoluteBlockPos.Directed abpp = travel.inFlight() ? FlightUtil.getPositionFromPercentage(travel.getPosition(), travel.getDestination(), tardis.getHandlers().getFlight().getDurationAsPercentage()) : travel.getPosition();

		String positionPosText = " " + abpp.getX() + ", " + abpp.getY() + ", " + abpp.getZ();
		String positionDimensionText = " " + DimensionControl.convertWorldValueToModified(abpp.getDimension().getValue());
		String positionDirectionText = " " + DirectionControl.rotationToDirection(abpp.getRotation()).toUpperCase();

		this.textRenderer.drawWithOutline(Text.of("❌").asOrderedText(), 0, 0, 0xF00F00, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
		this.textRenderer.drawWithOutline(Text.of(positionPosText).asOrderedText(), 0, 8, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
		this.textRenderer.drawWithOutline(Text.of(positionDimensionText).asOrderedText(), 0, 16, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
		this.textRenderer.drawWithOutline(Text.of(positionDirectionText).asOrderedText(), 0, 24, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);

		AbsoluteBlockPos.Directed abpd = tardis.travel().destination();

		String destinationPosText = " " + abpd.getX() + ", " + abpd.getY() + ", " + abpd.getZ();
		String destinationDimensionText = " " + DimensionControl.convertWorldValueToModified(abpd.getDimension().getValue());
		String destinationDirectionText = " " + DirectionControl.rotationToDirection(abpd.getRotation()).toUpperCase();

		this.textRenderer.drawWithOutline(Text.of("✛").asOrderedText(), 0, 40, 0x00F0FF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
		this.textRenderer.drawWithOutline(Text.of(destinationPosText).asOrderedText(), 0, 48, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
		this.textRenderer.drawWithOutline(Text.of(destinationDimensionText).asOrderedText(), 0, 56, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
		this.textRenderer.drawWithOutline(Text.of(destinationDirectionText).asOrderedText(), 0, 64, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);

		String fuelText = Math.round((tardis.getFuel() / FuelData.TARDIS_MAX_FUEL) * 100) + "%";
		this.textRenderer.drawWithOutline(Text.of("⛽").asOrderedText(), 0, 78, 0xFAF000, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
		this.textRenderer.drawWithOutline(Text.of(fuelText).asOrderedText(), 8, 78, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);

		String flightTimeText = tardis.travel().getState() == TardisTravel.State.LANDED ? "0%" : tardis.flight().getDurationAsPercentage() + "%";
		this.textRenderer.drawWithOutline(Text.of("⏳").asOrderedText(), 0, 92, 0x00FF0F, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);
		this.textRenderer.drawWithOutline(Text.of(flightTimeText).asOrderedText(), 8, 92, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);

		String name = tardis.stats().getName();
		this.textRenderer.drawWithOutline(Text.of(name).asOrderedText(), 98 - (this.textRenderer.getWidth(name)), 90, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);

		if (tardis.alarm().isEnabled())
			this.textRenderer.drawWithOutline(Text.of("⚠").asOrderedText(), 84, 0, 0xFE0000, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);

		matrices.pop();
	}
}