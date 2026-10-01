package com.umbralis.entity.boss;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.LargeFireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * JEFE 1 — Coloso de Obsidiana.
 *  - Coraza frontal: recibe solo el 15% del daño de frente. La ESPALDA recibe daño completo.
 *  - Golpe sísmico: tras un aviso, un impacto que debes esquivar SALTANDO. Después queda aturdido
 *    con el NÚCLEO EXPUESTO (x2.5 de daño desde cualquier ángulo).
 *  - Fase 2 (<66%): lluvia de meteoros. Fase 3 (<33%): más rápido, onda sísmica mayor y más meteoros.
 */
public class ObsidianColossusEntity extends AbstractUmbralBoss {
    private int stunTicks;
    private int slamWindup;

    public ObsidianColossusEntity(EntityType<? extends ObsidianColossusEntity> type, World world) {
        super(type, world, BossBar.Color.RED);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return baseAttributes(420.0, 16.0, 0.26);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new BossMeleeGoal(this, 1.0, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }

    @Override
    public boolean isIncapacitated() {
        return stunTicks > 0 || slamWindup > 0;
    }

    @Override
    protected void onPhaseChange(int newPhase) {
        super.onPhaseChange(newPhase);
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(newPhase == 2 ? 0.29 : 0.33);
    }

    @Override
    protected void bossTick() {
        ServerWorld sw = (ServerWorld) this.getWorld();
        if (stunTicks > 0) {
            stunTicks--;
            freeze();
            if (this.age % 4 == 0) {
                sw.spawnParticles(ParticleTypes.END_ROD, getX(), getBodyY(0.62), getZ(), 4, 0.4, 0.4, 0.4, 0.02);
            }
            return;
        }
        if (slamWindup > 0) {
            slamWindup--;
            freeze();
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.2, getZ(), 6, 1.2, 0.1, 1.2, 0.02);
            if (slamWindup == 0) {
                doSlam(sw);
            }
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null) return;
        if (abilityCooldown > 0) {
            abilityCooldown--;
            return;
        }
        boolean useMeteors = phase >= 2 && this.random.nextBoolean();
        if (useMeteors) {
            meteorShower();
            abilityCooldown = phase >= 3 ? 70 : 110;
        } else if (this.squaredDistanceTo(target) < 144.0) {
            slamWindup = 30;
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.HOSTILE, 3.0f, 0.5f);
            tell(Text.translatable("boss.umbralis.slam_incoming"), 40);
            abilityCooldown = phase >= 3 ? 70 : 100;
        } else {
            abilityCooldown = 20;
        }
    }

    private void freeze() {
        this.getNavigation().stop();
        Vec3d v = this.getVelocity();
        this.setVelocity(0.0, v.y, 0.0);
    }

    private void doSlam(ServerWorld sw) {
        double radius = phase >= 3 ? 12.0 : 9.0;
        for (PlayerEntity p : playersNear(radius)) {
            if (p.isOnGround()) {
                p.damage(this.getDamageSources().mobAttack(this), 14.0f);
            }
            Vec3d dir = p.getPos().subtract(this.getPos());
            dir = new Vec3d(dir.x, 0, dir.z).normalize();
            p.setVelocity(dir.x * 1.1, 0.9, dir.z * 1.1);
            p.velocityModified = true;
        }
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI * 2 / 24;
            for (double r = 2; r <= radius; r += 2.5) {
                sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX() + Math.cos(a) * r, getY() + 0.3, getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0.02);
            }
        }
        sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0f, 0.6f);
        stunTicks = phase >= 3 ? 80 : 100;
        tell(Text.translatable("boss.umbralis.core_exposed"), 40);
    }

    private void meteorShower() {
        int per = phase >= 3 ? 5 : 3;
        for (PlayerEntity p : playersNear(32.0)) {
            for (int i = 0; i < per; i++) {
                LargeFireballEntity f = new LargeFireballEntity(this.getWorld(), this, 0.0, -1.0, 0.0, 1);
                f.setPosition(p.getX() + this.random.nextGaussian() * 4.0, p.getY() + 14.0, p.getZ() + this.random.nextGaussian() * 4.0);
                this.getWorld().spawnEntity(f);
            }
        }
    }

    @Override
    protected float modifyIncomingDamage(DamageSource source, float amount) {
        if (stunTicks > 0) {
            burst(ParticleTypes.CRIT, 10, 0.5);
            return amount * 2.5f;
        }
        if (isFromBehind(source)) {
            burst(ParticleTypes.CRIT, 4, 0.3);
            return amount;
        }
        return amount * 0.15f;
    }
}
