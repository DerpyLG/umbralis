package com.umbralis.registry;

import com.umbralis.Umbralis;
import com.umbralis.entity.boss.EclipseIllusionEntity;
import com.umbralis.entity.boss.EclipseMonarchEntity;
import com.umbralis.entity.boss.ObsidianColossusEntity;
import com.umbralis.entity.boss.VoidWeaverEntity;
import com.umbralis.entity.boss.WebShotEntity;
import com.umbralis.entity.mob.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;

public class ModEntities {
    // ---- Hostiles ----
    public static final EntityType<UmbralStalkerEntity> STALKER = reg("umbral_stalker",
            FabricEntityTypeBuilder.<UmbralStalkerEntity>createMob()
                    .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                            HostileEntity::canSpawnIgnoreLightLevel)
                    .spawnGroup(SpawnGroup.MONSTER)
                    .entityFactory(UmbralStalkerEntity::new)
                    .dimensions(EntityDimensions.fixed(0.7f, 2.2f))
                    .trackRangeBlocks(64)
                    .build());

    public static final EntityType<UmbralWraithEntity> WRAITH = reg("umbral_wraith",
            FabricEntityTypeBuilder.<UmbralWraithEntity>createMob()
                    .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                            HostileEntity::canSpawnIgnoreLightLevel)
                    .spawnGroup(SpawnGroup.MONSTER)
                    .entityFactory(UmbralWraithEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .trackRangeBlocks(64)
                    .build());

    public static final EntityType<PurpurCrawlerEntity> CRAWLER = reg("purpur_crawler",
            FabricEntityTypeBuilder.<PurpurCrawlerEntity>createMob()
                    .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                            HostileEntity::canSpawnIgnoreLightLevel)
                    .spawnGroup(SpawnGroup.MONSTER)
                    .entityFactory(PurpurCrawlerEntity::new)
                    .dimensions(EntityDimensions.fixed(1.4f, 0.9f))
                    .trackRangeBlocks(64)
                    .build());

    public static final EntityType<VoidHatchlingEntity> HATCHLING = reg("void_hatchling",
            FabricEntityTypeBuilder.<VoidHatchlingEntity>createMob()
                    .spawnGroup(SpawnGroup.MONSTER)
                    .entityFactory(VoidHatchlingEntity::new)
                    .dimensions(EntityDimensions.fixed(0.9f, 0.6f))
                    .trackRangeBlocks(64)
                    .build());

    // ---- Amigables ----
    public static final EntityType<UmbralBisonEntity> BISON = reg("umbral_bison",
            FabricEntityTypeBuilder.<UmbralBisonEntity>createMob()
                    .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                            ModEntities::friendlySpawn)
                    .spawnGroup(SpawnGroup.CREATURE)
                    .entityFactory(UmbralBisonEntity::new)
                    .dimensions(EntityDimensions.fixed(0.9f, 1.4f))
                    .trackRangeBlocks(64)
                    .build());

    public static final EntityType<CrystalPigEntity> CRYSTAL_PIG = reg("crystal_pig",
            FabricEntityTypeBuilder.<CrystalPigEntity>createMob()
                    .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                            ModEntities::friendlySpawn)
                    .spawnGroup(SpawnGroup.CREATURE)
                    .entityFactory(CrystalPigEntity::new)
                    .dimensions(EntityDimensions.fixed(0.9f, 0.9f))
                    .trackRangeBlocks(64)
                    .build());

    public static final EntityType<PilgrimEntity> PILGRIM = reg("pilgrim",
            FabricEntityTypeBuilder.<PilgrimEntity>createMob()
                    .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                            ModEntities::friendlySpawn)
                    .spawnGroup(SpawnGroup.CREATURE)
                    .entityFactory(PilgrimEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .trackRangeBlocks(64)
                    .build());

    // ---- Jefes ----
    public static final EntityType<ObsidianColossusEntity> OBSIDIAN_COLOSSUS = reg("obsidian_colossus",
            FabricEntityTypeBuilder.<ObsidianColossusEntity>createMob()
                    .spawnGroup(SpawnGroup.MONSTER)
                    .entityFactory(ObsidianColossusEntity::new)
                    .dimensions(EntityDimensions.fixed(1.9f, 5.4f))
                    .fireImmune()
                    .trackRangeBlocks(160)
                    .build());

    public static final EntityType<VoidWeaverEntity> VOID_WEAVER = reg("void_weaver",
            FabricEntityTypeBuilder.<VoidWeaverEntity>createMob()
                    .spawnGroup(SpawnGroup.MONSTER)
                    .entityFactory(VoidWeaverEntity::new)
                    .dimensions(EntityDimensions.fixed(4.0f, 2.6f))
                    .trackRangeBlocks(160)
                    .build());

    public static final EntityType<EclipseMonarchEntity> ECLIPSE_MONARCH = reg("eclipse_monarch",
            FabricEntityTypeBuilder.<EclipseMonarchEntity>createMob()
                    .spawnGroup(SpawnGroup.MONSTER)
                    .entityFactory(EclipseMonarchEntity::new)
                    .dimensions(EntityDimensions.fixed(1.3f, 3.96f))
                    .fireImmune()
                    .trackRangeBlocks(160)
                    .build());

    public static final EntityType<EclipseIllusionEntity> ILLUSION = reg("eclipse_illusion",
            FabricEntityTypeBuilder.<EclipseIllusionEntity>createMob()
                    .spawnGroup(SpawnGroup.MONSTER)
                    .entityFactory(EclipseIllusionEntity::new)
                    .dimensions(EntityDimensions.fixed(1.3f, 3.96f))
                    .trackRangeBlocks(96)
                    .build());

    // ---- Proyectil ----
    public static final EntityType<WebShotEntity> WEB_SHOT = reg("web_shot",
            FabricEntityTypeBuilder.<WebShotEntity>create(SpawnGroup.MISC, WebShotEntity::new)
                    .dimensions(EntityDimensions.fixed(0.35f, 0.35f))
                    .trackRangeBlocks(4)
                    .trackedUpdateRate(10)
                    .build());

    private static <T extends Entity> EntityType<T> reg(String name, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, Umbralis.id(name), type);
    }

    /** Los mobs amigables aparecen sobre musgo etéreo, piedra umbral o purpur. */
    public static boolean friendlySpawn(EntityType<?> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        BlockState below = world.getBlockState(pos.down());
        return below.isOf(ModBlocks.ETHER_MOSS) || below.isOf(ModBlocks.UMBRAL_STONE) || below.isOf(Blocks.PURPUR_BLOCK);
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(STALKER, UmbralStalkerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(WRAITH, UmbralWraithEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CRAWLER, PurpurCrawlerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HATCHLING, VoidHatchlingEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(BISON, UmbralBisonEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CRYSTAL_PIG, CrystalPigEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(PILGRIM, PilgrimEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(OBSIDIAN_COLOSSUS, ObsidianColossusEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(VOID_WEAVER, VoidWeaverEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ECLIPSE_MONARCH, EclipseMonarchEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ILLUSION, EclipseIllusionEntity.createAttributes());
    }
}
