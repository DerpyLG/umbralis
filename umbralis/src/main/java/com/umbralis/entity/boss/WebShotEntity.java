package com.umbralis.entity.boss;

import com.umbralis.entity.mob.VoidHatchlingEntity;
import com.umbralis.registry.ModEntities;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

/** Red del Vacío: al impactar ralentiza y envuelve la zona en telarañas. */
public class WebShotEntity extends ThrownItemEntity {
    public WebShotEntity(EntityType<? extends WebShotEntity> type, World world) {
        super(type, world);
    }

    public WebShotEntity(World world, LivingEntity owner) {
        super(ModEntities.WEB_SHOT, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected float getGravity() {
        return 0.01f;
    }

    @Override
    protected void onEntityHit(EntityHitResult result) {
        super.onEntityHit(result);
        Entity e = result.getEntity();
        if (e instanceof AbstractUmbralBoss || e instanceof VoidHatchlingEntity) return;
        if (e instanceof LivingEntity living) {
            living.damage(this.getDamageSources().thrown(this, this.getOwner()), 5.0f);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        World world = this.getWorld();
        if (!world.isClient) {
            if (world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                BlockPos c = this.getBlockPos();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos p = c.add(dx, dy, dz);
                            if (world.getBlockState(p).isAir()) {
                                world.setBlockState(p, Blocks.COBWEB.getDefaultState());
                            }
                        }
                    }
                }
            }
            this.discard();
        }
    }
}
