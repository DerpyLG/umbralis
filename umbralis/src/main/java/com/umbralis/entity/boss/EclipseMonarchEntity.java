package com.umbralis.entity.boss;

import com.umbralis.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * JEFE 3 — Monarca del Eclipse.
 *  - Su cuerpo es casi inmune (x0.5; x0.25 si hay reflejos). PUNTOS DÉBILES:
 *      · CABEZA: flechas/tridentes que impactan en la parte alta hacen x2.5.
 *      · CAÍDA: golpes cuerpo a cuerpo con crítico (cayendo) hacen x1.2 incluso con reflejos.
 *      · CORAZÓN EXPUESTO: tras la Supernova queda aturdido (x2.5 desde cualquier lado).
 *  - Habilidades: Paso Umbral (teletransporte), Reflejos (3 clones; el real emite luz de corona),
 *    Gravedad Invertida (levitación masiva, fase 2+), Agujero Negro + Supernova (fase 3).
 */
public class EclipseMonarchEntity extends AbstractUmbralBoss {
    private final List<UUID> illusions = new ArrayList<>();
    private int stunTicks;
    private int blackHoleTicks;

    public EclipseMonarchEntity(EntityType<? extends EclipseMonarchEntity> type, World world) {
        super(type, world, BossBar.Color.PURPLE);
        this.experiencePoints = 1500;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return baseAttributes(520.0, 14.0, 0.30);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new BossMeleeGoal(this, 1.1, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 32.0f));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }

    @Override
    public boolean isIncapacitated() {
        return stunTicks > 0 || blackHoleTicks > 0;
    }

    @Override
    protected void bossTick() {
        ServerWorld sw = (ServerWorld) this.getWorld();
        illusions.removeIf(id -> {
            Entity e = sw.getEntity(id);
            return e == null || !e.isAlive();
        });

        if (this.age % 10 == 0) {
            sw.spawnParticles(ParticleTypes.END_ROD, getX(), getY() + getHeight() + 0.4, getZ(), 3, 0.3, 0.1, 0.3, 0.01);
        }

        if (stunTicks > 0) {
            stunTicks--;
            this.getNavigation().stop();
            if (this.age % 4 == 0) {
                sw.spawnParticles(ParticleTypes.END_ROD, getX(), getBodyY(0.62), getZ(), 4, 0.4, 0.4, 0.4, 0.02);
            }
            return;
        }

        if (blackHoleTicks > 0) {
            blackHoleTicks--;
            this.getNavigation().stop();
            Vec3d center = this.getPos().add(0, this.getHeight() * 0.5, 0);
            for (PlayerEntity p : playersNear(22.0)) {
                Vec3d d = center.subtract(p.getPos());
                if (d.length() < 1.5) continue;
                Vec3d pull = d.normalize().multiply(0.18);
                p.addVelocity(pull.x, pull.y * 0.5, pull.z);
                p.velocityModified = true;
                if (blackHoleTicks % 10 == 0 && d.length() < 4.0) {
                    p.damage(this.getDamageSources().mobAttack(this), 4.0f);
                }
            }
            sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, getX(), getBodyY(0.5), getZ(), 14, 6.0, 3.0, 6.0, 0.05);
            if (blackHoleTicks == 0) supernova(sw);
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null) return;
        if (abilityCooldown > 0) {
            abilityCooldown--;
            return;
        }
        int roll = this.random.nextInt(10);
        if (phase >= 3 && roll < 4) {
            blackHoleTicks = 80;
            tell(Text.translatable("boss.umbralis.monarch_blackhole"), 40);
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 4.0f, 0.5f);
        } else if (phase >= 2 && roll < 7) {
            gravityInversion();
        } else if (illusions.isEmpty() && roll < 8) {
            summonMirrors(sw, target);
        } else {
            blinkNear(sw, target);
        }
        abilityCooldown = 80 - 15 * (phase - 1);
    }

    private void blinkNear(ServerWorld sw, LivingEntity target) {
        for (int i = 0; i < 10; i++) {
            double a = this.random.nextDouble() * Math.PI * 2;
            double d = 4.0 + this.random.nextDouble() * 3.0;
            double x = target.getX() + Math.cos(a) * d;
            double z = target.getZ() + Math.sin(a) * d;
            double y = target.getY();
            Box box = this.getBoundingBox().offset(x - getX(), y - getY(), z - getZ());
            if (sw.isSpaceEmpty(this, box)) {
                sw.spawnParticles(ParticleTypes.PORTAL, getX(), getBodyY(0.5), getZ(), 30, 0.5, 1.0, 0.5, 0.1);
                this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
                this.getNavigation().stop();
                sw.spawnParticles(ParticleTypes.PORTAL, x, y + 1.5, z, 30, 0.5, 1.0, 0.5, 0.1);
                sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0f, 0.6f);
                return;
            }
        }
    }

    private void summonMirrors(ServerWorld sw, LivingEntity target) {
        for (int i = 0; i < 3; i++) {
            EclipseIllusionEntity e = ModEntities.ILLUSION.create(sw);
            if (e == null) continue;
            double a = this.random.nextDouble() * Math.PI * 2;
            e.refreshPositionAndAngles(getX() + Math.cos(a) * 3.5, getY(), getZ() + Math.sin(a) * 3.5, this.getYaw(), 0.0f);
            e.setTarget(target);
            sw.spawnEntity(e);
            illusions.add(e.getUuid());
        }
        tell(Text.translatable("boss.umbralis.monarch_mirrors"), 40);
        sw.playSound(null, getBlockPos(), SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.HOSTILE, 3.0f, 0.4f);
    }

    private void gravityInversion() {
        for (PlayerEntity p : playersNear(24.0)) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 50, 3));
        }
        burst(ParticleTypes.REVERSE_PORTAL, 60, 3.0);
        tell(Text.translatable("boss.umbralis.monarch_gravity"), 40);
    }

    private void supernova(ServerWorld sw) {
        for (PlayerEntity p : playersNear(10.0)) {
            p.damage(this.getDamageSources().mobAttack(this), 18.0f);
            Vec3d dir = p.getPos().subtract(this.getPos());
            dir = new Vec3d(dir.x, 0, dir.z).normalize();
            p.setVelocity(dir.x * 1.6, 0.8, dir.z * 1.6);
            p.velocityModified = true;
        }
        sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getBodyY(0.5), getZ(), 3, 1.5, 1.0, 1.5, 0.0);
        sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 4.0f, 0.5f);
        stunTicks = 100;
        tell(Text.translatable("boss.umbralis.monarch_exposed"), 40);
    }

    @Override
    protected float modifyIncomingDamage(DamageSource source, float amount) {
        if (stunTicks > 0) {
            burst(ParticleTypes.CRIT, 10, 0.5);
            return amount * 2.5f;
        }
        Entity direct = source.getSource();
        if (direct instanceof PersistentProjectileEntity && direct.getY() > this.getY() + this.getHeight() * 0.7) {
            burst(ParticleTypes.CRIT, 12, 0.3);
            this.getWorld().playSound(null, getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 1.0f, 0.7f);
            return amount * 2.5f;
        }
        float base = illusions.isEmpty() ? 0.5f : 0.25f;
        if (source.getAttacker() instanceof PlayerEntity p && p.fallDistance > 0.0f && !p.isOnGround()
                && !p.isClimbing() && !p.isTouchingWater()) {
            burst(ParticleTypes.CRIT, 6, 0.3);
            return amount * 1.2f;
        }
        return amount * base;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (this.getWorld() instanceof ServerWorld sw) {
            for (UUID id : illusions) {
                Entity e = sw.getEntity(id);
                if (e != null) e.discard();
            }
        }
    }
}
