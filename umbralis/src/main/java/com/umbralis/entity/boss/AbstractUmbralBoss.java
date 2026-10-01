package com.umbralis.entity.boss;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Base común: barra de jefe, fases por vida, cooldown de habilidades y daño modificable. */
public abstract class AbstractUmbralBoss extends HostileEntity {
    protected final ServerBossBar bossBar;
    protected int phase = 1;
    protected int abilityCooldown = 100;

    protected AbstractUmbralBoss(EntityType<? extends AbstractUmbralBoss> type, World world, BossBar.Color color) {
        super(type, world);
        this.bossBar = new ServerBossBar(this.getDisplayName(), color, BossBar.Style.NOTCHED_10);
        this.experiencePoints = 600;
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder baseAttributes(double health, double damage, double speed) {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, health)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, damage)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_ARMOR, 8.0);
    }

    /** Si es true, el jefe no se mueve ni ataca cuerpo a cuerpo. */
    public boolean isIncapacitated() {
        return false;
    }

    protected abstract void bossTick();

    protected float modifyIncomingDamage(DamageSource source, float amount) {
        return amount;
    }

    protected int computePhase() {
        float ratio = this.getHealth() / this.getMaxHealth();
        return ratio > 0.66f ? 1 : ratio > 0.33f ? 2 : 3;
    }

    protected void onPhaseChange(int newPhase) {
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 3.0f, 0.7f);
        tell(Text.translatable("boss.umbralis.phase", this.getDisplayName()), 64);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        int np = computePhase();
        if (np != phase) {
            phase = np;
            onPhaseChange(np);
        }
        bossTick();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.damage(source, amount);
        }
        if (source.getAttacker() == this) {
            return false;
        }
        float modified = modifyIncomingDamage(source, amount);
        if (modified <= 0.0f) {
            onDamageBlocked(source);
            return false;
        }
        return super.damage(source, modified);
    }

    protected void onDamageBlocked(DamageSource source) {
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 0.5f, 1.8f);
    }

    @Override
    public boolean tryAttack(Entity target) {
        return !isIncapacitated() && super.tryAttack(target);
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        super.takeKnockback(strength * 0.15, x, z);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public void setCustomName(Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.getWorld().isClient) {
            tell(Text.translatable("boss.umbralis.defeated", this.getDisplayName()), 64);
        }
    }

    // ---- utilidades ----

    protected List<PlayerEntity> playersNear(double radius) {
        return this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(radius),
                p -> p.isAlive() && !p.isSpectator() && p.squaredDistanceTo(this) <= radius * radius);
    }

    protected void tell(Text text, double radius) {
        for (PlayerEntity p : playersNear(radius)) {
            p.sendMessage(text, true);
        }
    }

    protected void burst(ParticleEffect particle, int count, double spread) {
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(particle, getX(), getBodyY(0.6), getZ(), count, spread, spread, spread, 0.05);
        }
    }

    /** ¿La fuente del daño está en el hemisferio trasero del jefe? */
    protected boolean isFromBehind(DamageSource source) {
        Entity src = source.getSource() != null ? source.getSource() : source.getAttacker();
        if (src == null) return false;
        Vec3d to = src.getPos().subtract(this.getPos());
        to = new Vec3d(to.x, 0, to.z);
        if (to.lengthSquared() < 1.0E-4) return false;
        Vec3d facing = Vec3d.fromPolar(0.0f, this.bodyYaw);
        return facing.dotProduct(to.normalize()) < -0.25;
    }

    /** Ataque cuerpo a cuerpo que respeta el estado de incapacitación. */
    public static class BossMeleeGoal extends MeleeAttackGoal {
        private final AbstractUmbralBoss boss;

        public BossMeleeGoal(AbstractUmbralBoss boss, double speed, boolean pauseWhenIdle) {
            super(boss, speed, pauseWhenIdle);
            this.boss = boss;
        }

        @Override
        public boolean canStart() {
            return !boss.isIncapacitated() && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return !boss.isIncapacitated() && super.shouldContinue();
        }
    }
}
