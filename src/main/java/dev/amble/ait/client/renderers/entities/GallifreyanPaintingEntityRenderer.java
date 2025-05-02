package dev.amble.ait.client.renderers.entities;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import dev.amble.ait.client.boti.BOTI;
import dev.amble.ait.core.entities.BOTIPaintingEntity;

@Environment(value=EnvType.CLIENT)
public class GallifreyanPaintingEntityRenderer
        extends EntityRenderer<BOTIPaintingEntity> {
    public GallifreyanPaintingEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(BOTIPaintingEntity paintingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        BOTI.GALLIFREYAN_RENDER_QUEUE.add(paintingEntity);
    }

    @Override
    public Identifier getTexture(BOTIPaintingEntity entity) {
        return null;
    }

}
