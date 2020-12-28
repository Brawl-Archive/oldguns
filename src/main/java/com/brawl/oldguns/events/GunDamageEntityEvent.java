package com.brawl.oldguns.events;

import com.brawl.oldguns.gun.Gun;
import com.brawl.oldguns.gun.GunPlayer;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EnchantmentManager;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.MobEffectList;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GunDamageEntityEvent extends GunEvent {
    private final Gun gun;
    private final GunPlayer shooter;
    private final Entity shot;
    private boolean isHeadshot;
    private int armorPenetration;
    private double damage;
    private EntityDamageByEntityEvent event;

    @Getter
    private int damagedArmorPoints = 0;

    public GunDamageEntityEvent(EntityDamageByEntityEvent event, GunPlayer shooter, Gun gun, Entity shot,
                                boolean headshot) {
        this.gun = gun;
        this.shooter = shooter;
        this.shot = shot;
        this.isHeadshot = headshot;
        this.damage = gun.getGunDamage();
        this.armorPenetration = gun.getArmorPenetration();

        if (shot instanceof LivingEntity) {
            LivingEntity lent = (LivingEntity) shot;
            EntityLiving el = ((CraftLivingEntity) lent).getHandle();
            this.damagedArmorPoints = el.br();
        }
    }

    public EntityDamageByEntityEvent getEntityDamageEntityEvent() {
        return event;
    }

    public boolean isHeadshot() {
        return isHeadshot;
    }

    public void setHeadshot(boolean b) {
        this.isHeadshot = b;
    }

    public GunPlayer getShooter() {
        return shooter;
    }

    public Entity getEntityDamaged() {
        return shot;
    }

    public Player getKillerAsPlayer() {
        return shooter.getPlayer();
    }

    public Gun getGun() {
        return gun;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public int getArmorPenetration() {
        return armorPenetration;
    }

    public void setArmorPenetration(int armorPenetration) {
        this.armorPenetration = armorPenetration;
    }

    /**
     * @return Final damage dealt
     */
    public final double getFinalDamage() {
        if (!(shot instanceof LivingEntity))
            return damage;

        LivingEntity lent = (LivingEntity) shot;
        EntityLiving el = ((CraftLivingEntity) lent).getHandle();

        float dmg = (float) getDamage() * (isHeadshot ? 2 : 1);
        dmg = applyArmorModifier(dmg, damagedArmorPoints);
        dmg = applyMagicModifiers(el, dmg);
        dmg += armorPenetration;

        return dmg;
    }

    private float applyMagicModifiers(EntityLiving origin, float f) {
        int i;
        int j;
        float f1;

        if (origin.hasEffect(MobEffectList.RESISTANCE)) {
            i = (origin.getEffect(MobEffectList.RESISTANCE).getAmplifier() + 1) * 5;
            j = 25 - i;
            f1 = f * j;
            f = f1 / 25.0F;
        }

        if (f <= 0.0F) {
            return 0.0F;
        } else {
            i = EnchantmentManager.a(origin.getEquipment(), DamageSource.GENERIC);
            if (i > 20) {
                i = 20;
            }

            if (i > 0 && i <= 20) {
                j = 25 - i;
                f1 = f * j;
                f = f1 / 25.0F;
            }

            return f;
        }
    }

    private float applyArmorModifier(float f, int br) {
        int i = 25 - br;
        float f1 = f * i;

        f = f1 / 25.0F;

        return f;
    }
}
