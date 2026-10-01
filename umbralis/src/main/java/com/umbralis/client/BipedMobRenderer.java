package com.umbralis.client;

import com.umbralis.Umbralis;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class BipedMobRenderer<T extends MobEntity> extends BipedEntityRenderer<T, BipedEntityModel<T>> {
    private final Identifier texture;
    private final float scale;

    public BipedMobRenderer(EntityRendererFactory.Context ctx, String textureName, float scale) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f * scale);
        this.texture = Umbralis.id("textures/entity/" + textureName + ".png");
        this.scale = scale;
    }

    @Override
    public Identifier getTexture(T entity) {
        return texture;
    }

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        matrices.scale(scale, scale, scale);
    }
}
