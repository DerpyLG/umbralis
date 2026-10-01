package com.umbralis.client;

import com.umbralis.Umbralis;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class SimpleMobRenderer<T extends MobEntity, M extends EntityModel<T>> extends MobEntityRenderer<T, M> {
    private final Identifier texture;
    private final float scale;

    public SimpleMobRenderer(EntityRendererFactory.Context ctx, M model, float shadow, String textureName, float scale) {
        super(ctx, model, shadow * scale);
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
