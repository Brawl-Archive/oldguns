package com.orange451.pvpgunplus.gun.util;

import com.brawl.base.util.*;
import com.google.common.collect.*;
import lombok.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.*;

import java.util.*;
import java.util.function.*;

@Data
public class CustomExplosion {

    private static final DamageSource damageSource = DamageSource.explosion(null);

    // --------------===[ Builder ]===--------------
    private final List<BlockPosition> blocks = Lists.newArrayList();
    private final Map<EntityHuman, Vec3D> players = Maps.newHashMap();
    private final Set<Consumer<ExplosionDamageData>> localPreDamageListeners = new HashSet<>(3);
    private final Set<Consumer<ExplosionDamageData>> localPostDamageListeners = new HashSet<>(3);
    private final Set<Consumer<org.bukkit.block.Block>> localIgniteListeners = new HashSet<>(3);
    private net.minecraft.server.v1_8_R3.World world;
    private double posX;
    private double posY;
    private double posZ;
    private double size;
    private double fireChance = 0.3;
    private boolean breakBlocks = true;
    private boolean damageEntities = true;
    private boolean chainExplosions = true;
    private boolean showParticles = true;

    //	@Setter
    //	private GunExplosionEvent parentEvent;
    private boolean ignoreExposure = false;
    private boolean silent = false;
    private float dropYield = 0.3f;
    private Predicate<org.bukkit.block.Block> ignitePredicate;

    public CustomExplosion(net.minecraft.server.v1_8_R3.World world, double posX, double posY, double posZ) {
        this.world = world;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public static CustomExplosion at(Location location) {
        return new CustomExplosion(
                ((CraftWorld) location.getWorld()).getHandle(),
                location.getX(),
                location.getY(),
                location.getZ());
    }

    // --------------===[ Listener methods ]===--------------

    public void preDamageListener(Consumer<ExplosionDamageData> c) {
        localPreDamageListeners.add(c);
    }

    public void postDamageListener(Consumer<ExplosionDamageData> c) {
        localPostDamageListeners.add(c);
    }

    public void igniteListener(Consumer<org.bukkit.block.Block> c) {
        localIgniteListeners.add(c);
    }

    public void unregisterListener(Consumer<?> c) {
        localIgniteListeners.remove(c);
        localPreDamageListeners.remove(c);
        localPostDamageListeners.remove(c);
    }

    // --------------===[ Explosion methods ]===--------------

    public void boom() {
        Location fixedLoc = ExplosionsUtil.getFixedLocation(this);

        world = ((CraftWorld) fixedLoc.getWorld()).getHandle();
        posX = fixedLoc.getX();
        posY = fixedLoc.getY();
        posZ = fixedLoc.getZ();

        scanBlocks();
        breakBlockAndParticles();
        damageEntities();
        igniteBlocks();

        List<BlockPosition> destroyed = (breakBlocks) ? blocks : new ArrayList<>(0);

        for (EntityHuman player : world.players) {
            if (player.e(posX, posY, posZ) < 4096) {
                ((EntityPlayer) player).playerConnection.sendPacket(new PacketPlayOutExplosion(posX, posY, posZ, (float) size, destroyed, players.get(player)));
            }
        }
    }

    private void scanBlocks() {
        if (size < 0.1)
            return;

        Set<BlockPosition> toBeDestroyed = Sets.newHashSet();

        // -------==[ 1. Applies ray-tracing and determines which blocks to break ]==-------
        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
                        // All three values range from [-1, 1]
                        double rayX = x / 15.0F * 2 - 1;
                        double rayY = y / 15.0F * 2 - 1;
                        double rayZ = z / 15.0F * 2 - 1;
                        double length = Math.sqrt(rayX * rayX + rayY * rayY + rayZ * rayZ);

                        rayX /= length;
                        rayY /= length;
                        rayZ /= length;
                        double idk = this.size * (0.7 + this.world.random.nextFloat() * 0.6);
                        double blockX = this.posX;
                        double blockY = this.posY;
                        double blockZ = this.posZ;

                        for (; idk > 0.0F; idk -= 0.225F) {
                            BlockPosition block = new BlockPosition(blockX, blockY, blockZ);
                            IBlockData blockData = this.world.getType(block);

                            if (blockData.getBlock().getMaterial() != net.minecraft.server.v1_8_R3.Material.AIR) {
                                float idk3 = blockData.getBlock().a((net.minecraft.server.v1_8_R3.Entity) null);

                                idk -= (idk3 + 0.3F) * 0.3F;
                            }

                            if (idk > 0.0F && block.getY() < 256 && block.getY() >= 0) {
                                toBeDestroyed.add(block);
                            }

                            blockX += rayX * 0.3;
                            blockY += rayY * 0.3;
                            blockZ += rayZ * 0.3;
                        }
                    }
                }
            }
        }

        this.blocks.addAll(toBeDestroyed);
    }

    private void breakBlockAndParticles() {
        if (!silent) {
            // Pitch varies between [0.5 ; 0.85)
            float pitch = (world.random.nextFloat() * 0.35F) + 0.5F;
            this.world.makeSound(this.posX, this.posY, this.posZ, "random.explode", 4, pitch);
        }

        EnumParticle particle = (size >= 2) ? EnumParticle.EXPLOSION_HUGE : EnumParticle.EXPLOSION_LARGE;
        this.world.addParticle(particle, this.posX, this.posY, this.posZ, 1, 0, 0);

        for (BlockPosition pos : blocks) {
            Block block = this.world.getType(pos).getBlock();

            world.spigotConfig.antiXrayInstance.updateNearbyBlocks(world, pos);
            if (showParticles) {
                double partX = pos.getX() + this.world.random.nextFloat();
                double partY = pos.getY() + this.world.random.nextFloat();
                double partZ = pos.getZ() + this.world.random.nextFloat();
                double offX = partX - this.posX;
                double offY = partY - this.posY;
                double offZ = partZ - this.posZ;
                double offLength = MathHelper.sqrt(offX * offX + offY * offY + offZ * offZ);

                offX /= offLength;
                offY /= offLength;
                offZ /= offLength;
                double idk = 0.5D / (offLength / this.size + 0.1D);

                idk *= this.world.random.nextFloat() * this.world.random.nextFloat() + 0.3F;
                offX *= idk;
                offY *= idk;
                offZ *= idk;
                this.world.addParticle(EnumParticle.EXPLOSION_NORMAL, (partX + this.posX * 1.0D) / 2.0D, (partY + this.posY * 1.0D) / 2.0D, (partZ + this.posZ * 1.0D) / 2.0D, offX, offY, offZ);
                this.world.addParticle(EnumParticle.SMOKE_NORMAL, partX, partY, partZ, offX, offY, offZ);
            }

            if (breakBlocks) {
                if (block.getMaterial() != net.minecraft.server.v1_8_R3.Material.AIR) {
                    if (block.getMaterial() == net.minecraft.server.v1_8_R3.Material.TNT) {
                        if (chainExplosions) {
                            EntityTNTPrimed tnt = new EntityTNTPrimed(world, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, null);
                            tnt.fuseTicks = world.random.nextInt(tnt.fuseTicks / 4) + tnt.fuseTicks / 8;
                            world.addEntity(tnt);
                        }
                    } else {
                        block.dropNaturally(this.world, pos, this.world.getType(pos), dropYield, 0);
                    }

                    this.world.setTypeAndData(pos, Blocks.AIR.getBlockData(), 3);
                }
            }
        }
    }

    private void damageEntities() {
        if (!damageEntities || size < 0.1)
            return;

        // -----------------==[ 2. Find and damage nearby entities ]==-----------------
        double damageSize = this.size * 2;
        damageMinecraftEntities(damageSize);
    }

    private void damageMinecraftEntities(double damageSize) {
        double grow = damageSize + 1;
        AxisAlignedBB aabb = new AxisAlignedBB(posX, posY, posZ, posX, posY, posZ).grow(grow, grow, grow);

        List<net.minecraft.server.v1_8_R3.Entity> entities = this.world.getEntities(null, aabb);
        Vec3D posVector = new Vec3D(this.posX, this.posY, this.posZ);

        for (net.minecraft.server.v1_8_R3.Entity entity : entities) {
            if (entity.aW())
                continue;

            double distancePercent = entity.f(this.posX, this.posY, this.posZ) / damageSize;
            if (distancePercent > 1)
                continue;

            double diffX = entity.locX - this.posX;
            double diffY = entity.locY + entity.getHeadHeight() - this.posY;
            double diffZ = entity.locZ - this.posZ;
            double length = MathHelper.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);

            if (length == 0)
                continue;

            diffX /= length;
            diffY /= length;
            diffZ /= length;
            double exposure = (ignoreExposure) ? 1 : this.world.a(posVector, entity.getBoundingBox());
            double impact = (1 - distancePercent) * exposure;

            float damage = ((int) ((impact * impact + impact) / 2.0D * 8.0D * damageSize + 1.0D));
            ExplosionDamageData entry = new ExplosionDamageData(this, entity.getBukkitEntity(), damage);

            localPreDamageListeners.forEach(cons -> cons.accept(entry));
            ExplosionsUtil.preDamageListeners.forEach(cons -> cons.accept(entry));

            boolean wasDamaged = damageEntity(entity, entry.getDamage());
            System.out.println("was damaged " + entity.getName() + ": " + wasDamaged);
            if (wasDamaged) {
                localPostDamageListeners.forEach(cons -> cons.accept(entry));
                ExplosionsUtil.postDamageListeners.forEach(cons -> cons.accept(entry));

            } else if (!(entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock)) {
                continue;
            }

            double reducedImpact = EnchantmentProtection.a(entity, impact);

            entity.motX += diffX * reducedImpact;
            entity.motY += diffY * reducedImpact;
            entity.motZ += diffZ * reducedImpact;
            if (entity instanceof EntityHuman && !((EntityHuman) entity).abilities.isInvulnerable) {
                this.players.put((EntityHuman) entity, new Vec3D(diffX * impact, diffY * impact, diffZ * impact));
            }
        }
    }

    private void igniteBlocks() {
        if (fireChance <= 0)
            return;

        org.bukkit.World bworld = this.world.getWorld();
        for (BlockPosition pos : blocks) {
            if (!Util.chance(fireChance))
                continue;

            org.bukkit.block.Block bblock = bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ());

            boolean valid;
            if (ignitePredicate != null) {
                valid = ignitePredicate.test(bblock);
            } else {
                // If is air & block below is opaque
                valid = (this.world.getType(pos).getBlock().getMaterial() == net.minecraft.server.v1_8_R3.Material.AIR && this.world.getType(pos.down()).getBlock().o());
            }

            if (valid) {
                this.world.setTypeUpdate(pos, Blocks.FIRE.getBlockData());

                localIgniteListeners.forEach(cons -> cons.accept(bblock));
                ExplosionsUtil.igniteListeners.forEach(cons -> cons.accept(bblock));
            }
        }
    }

    // --------------===[ Util ]===--------------

    private boolean damageEntity(Object e, double damage) {
        if (damage <= 0)
            return false;

        if (e instanceof net.minecraft.server.v1_8_R3.Entity) {
            net.minecraft.server.v1_8_R3.Entity entity = (net.minecraft.server.v1_8_R3.Entity) e;
            return entity.damageEntity(damageSource, (float) damage);
        } else if (e instanceof SimpleDamageable) {
            ((SimpleDamageable) e).damage(this, damage);
            return true;
        }

        return false;
    }

    @Data
    @AllArgsConstructor
    public static class ExplosionDamageData {
        private final CustomExplosion explosion;
        private final Object entity;
        private double damage;

        public void cancel() {
            damage = 0;
        }
    }

}