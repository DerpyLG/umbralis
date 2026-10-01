package com.umbralis.client;

import com.umbralis.registry.ModBlocks;
import com.umbralis.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.entity.mob.MobEntity;

public class UmbralisClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UMBRAL_PORTAL, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ETHER_FLOWER, RenderLayer.getCutout());

        EntityRendererRegistry.register(ModEntities.STALKER, biped("umbral_stalker", 1.0f));
        EntityRendererRegistry.register(ModEntities.WRAITH, biped("umbral_wraith", 1.0f));
        EntityRendererRegistry.register(ModEntities.PILGRIM, biped("pilgrim", 1.0f));
        EntityRendererRegistry.register(ModEntities.OBSIDIAN_COLOSSUS, biped("obsidian_colossus", 3.0f));
        EntityRendererRegistry.register(ModEntities.ECLIPSE_MONARCH, biped("eclipse_monarch", 2.2f));
        EntityRendererRegistry.register(ModEntities.ILLUSION, biped("eclipse_monarch", 2.2f));

        EntityRendererRegistry.register(ModEntities.CRAWLER, spider("purpur_crawler", 1.0f));
        EntityRendererRegistry.register(ModEntities.HATCHLING, spider("purpur_crawler", 0.7f));
        EntityRendererRegistry.register(ModEntities.VOID_WEAVER, spider("void_weaver", 3.0f));

        EntityRendererRegistry.register(ModEntities.BISON, cow("umbral_bison"));
        EntityRendererRegistry.register(ModEntities.CRYSTAL_PIG, pig("crystal_pig"));

        EntityRendererRegistry.register(ModEntities.WEB_SHOT, FlyingItemEntityRenderer::new);
    }

    private static <T extends MobEntity> EntityRendererFactory<T> biped(String texture, float scale) {
        return ctx -> new BipedMobRenderer<>(ctx, texture, scale);
    }

    private static <T extends MobEntity> EntityRendererFactory<T> spider(String texture, float scale) {
        return ctx -> new SimpleMobRenderer<T, SpiderEntityModel<T>>(ctx,
                new SpiderEntityModel<T>(ctx.getPart(EntityModelLayers.SPIDER)), 1.0f, texture, scale);
    }

    private static <T extends MobEntity> EntityRendererFactory<T> cow(String texture) {
        return ctx -> new SimpleMobRenderer<T, CowEntityModel<T>>(ctx,
                new CowEntityModel<T>(ctx.getPart(EntityModelLayers.COW)), 0.7f, texture, 1.0f);
    }

    private static <T extends MobEntity> EntityRendererFactory<T> pig(String texture) {
        return ctx -> new SimpleMobRenderer<T, PigEntityModel<T>>(ctx,
                new PigEntityModel<T>(ctx.getPart(EntityModelLayers.PIG)), 0.7f, texture, 1.0f);
    }
}
