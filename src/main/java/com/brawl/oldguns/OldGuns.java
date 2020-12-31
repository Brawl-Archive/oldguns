package com.brawl.oldguns;

import com.brawl.base.BrawlPlayer;
import com.brawl.base.BrawlPlugin;
import com.brawl.base.util.scheduler.Sync;
import com.brawl.oldguns.commands.EnableTestingCommand;
import com.brawl.oldguns.commands.OldGunsCommand;
import com.brawl.oldguns.commands.TestingCommand;
import com.brawl.oldguns.gun.*;
import com.brawl.oldguns.listeners.PluginEntityListener;
import com.brawl.oldguns.listeners.PluginPlayerListener;
import com.brawl.shared.chat.C;
import com.brawl.shared.network.message.NetworkChannel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

@Getter
@Setter
public class OldGuns extends JavaPlugin {
    private static OldGuns plugin;
    private final PluginPlayerListener playerListener = new PluginPlayerListener(this);
    private final PluginEntityListener entityListener = new PluginEntityListener(this);
    private final ArrayList<Gun> loadedGuns = new ArrayList<>();
    private final ArrayList<EffectType> effects = new ArrayList<>();
    //private final ArrayList<GunPlayer> players = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private int UpdateTimer;
    private Random random;

    public static void resetPlayerDamage(LivingEntity entity, int ticks) {
        CraftLivingEntity craftLiving = ((CraftLivingEntity) entity);
        EntityLiving minecraftLivingEntity = craftLiving.getHandle();

        // Reset damage ticks directly. TEMP FIX //////////
        minecraftLivingEntity.maxNoDamageTicks = ticks; //
        minecraftLivingEntity.noDamageTicks = ticks; //
        minecraftLivingEntity.hurtTicks = ticks; //
        minecraftLivingEntity.lastDamage = -1;     //
        ///////////////////////////////////////////////////

        entity.setLastDamage(ticks);
        entity.setNoDamageTicks(ticks);
        entity.setLastDamageCause(null);
    }

    public static void playEffect(Effect e, Location l, int num) {
        for (Player player : Bukkit.getServer().getOnlinePlayers())
            player.playEffect(l, e, num);
    }

    public static OldGuns getInstance() {
        return plugin;
    }

    public static Sound getSound(String gunSound) {
        String snd = gunSound.toUpperCase().replace(" ", "_");
        return Sound.valueOf(snd);
    }

    public void onDisable() {
        System.out.println("OldGuns disabled");
        clearMemory(true);
    }

    public void onEnable() {
        System.out.println("OldGuns enabled");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.playerListener, this);
        pm.registerEvents(this.entityListener, this);
        new TestingCommand().registerCommand();
        new EnableTestingCommand().registerCommand();
        new OldGunsCommand().registerCommand();
        GunPlayer.initMeta();
        GunTests.init();
        BrawlPlugin.getInstance().getClientManager().onCreate(client -> {
            client.onInput(NetworkChannel.DATA, GunTests.ActivateMessage.class, activate -> {
                activate.getTest().set(true, null);
                BrawlPlayer.getOnlinePlayers(BrawlPlayer::isStaff).forEach(player -> {
                    player.sendMessage(C.warn(C.BLUE) + "Test " + C.highlight(activate.getTest().name()) + " has been activated by " + C.highlight(activate.getActivatedBy()) + "!");
                });
            });
        });

        startup(true);

        Sync.get().interval(1).run(() -> {
            for (PluginEntityListener.PStat stat : entityListener.stackedDamage.values()) {
                if (stat.getStackedDamage().isEmpty())
                    continue;

                double damage = stat.getStackedDamage().poll();
                ((CraftLivingEntity) stat.getLent()).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, (float) damage);
            }
        });
    }

    public void clearMemory(boolean init) {
        getServer().getScheduler().cancelTask(this.UpdateTimer);
        for (Bullet b : bullets) {
            b.destroy();
        }
        GunPlayer.meta.forEach(GunPlayer::unload);
        if (init) {
            loadedGuns.clear();
        }
        bullets.clear();
        //players.clear();
    }

    public void startup(boolean init) {
        this.UpdateTimer = getServer().getScheduler().scheduleSyncRepeatingTask(this, new UpdateTimer(), 20L, 1L);

        this.random = new Random();
        OldGuns.plugin = this;

        File dir = new File(getPluginFolder());
        if (!dir.exists()) {
            dir.mkdir();
        }

        File dir2 = new File(getPluginFolder() + "/guns");
        if (!dir2.exists()) {
            dir2.mkdir();
        }

        dir2 = new File(getPluginFolder() + "/projectile");
        if (!dir2.exists()) {
            dir2.mkdir();
        }

        if (init) {
            loadGuns();
            loadProjectile();
        }

        getOnlinePlayers();
    }

    private String getPluginFolder() {
        File file = getDataFolder();
        file.mkdir();
        return file.getAbsolutePath();
    }

    private void loadProjectile() {
        String path = getPluginFolder() + "/projectile";
        File dir = new File(path);
        String[] children = dir.list();
        if (children != null) {
            for (String filename : children) {
                WeaponReader f = new WeaponReader(this, new File(path + "/" + filename), "gun");
                if (f.loaded) {
                    f.ret.setNode("pvpgunplus." + filename.toLowerCase());
                    this.loadedGuns.add(f.ret);
                    f.ret.setThrowable(true);
                    System.out.println("LOADED PROJECTILE: " + f.ret.getGunName());
                } else {
                    System.out.println("FAILED TO PROJECTILE GUN: " + f.ret.getGunName());
                }
            }
        }
    }

    private void loadGuns() {
        String path = getPluginFolder() + "/guns";
        File dir = new File(path);
        String[] children = dir.list();
        if (children != null) {
            for (String filename : children) {
                WeaponReader f = new WeaponReader(this, new File(path + "/" + filename), "gun");
                if (f.loaded) {
                    f.ret.setNode("pvpgunplus." + filename.toLowerCase());
                    this.loadedGuns.add(f.ret);
                    System.out.println("LOADED GUN: " + f.ret.getGunName());
                } else {
                    System.out.println("FAILED TO LOAD GUN: " + f.ret.getGunName());
                }
            }
        }
    }

    public void reload(boolean b) {
        clearMemory(b);
        startup(b);
    }

    public void reload() {
        reload(false);
    }

    public void getOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GunPlayer g = new GunPlayer(this, player);
            if (GunPlayer.get(player) == null)
                GunPlayer.meta.put(player, g);
        }
    }

    public GunPlayer getGunPlayer(Player player) {
        return GunPlayer.get(player);
    }

    public Gun getGun(int typeId) {
        for (Gun g : loadedGuns) {
            if (g.getGunMaterial() != null) {
                if (g.getGunMaterial().getId() == typeId) {
                    return g;
                }
            }
        }
        return null;
    }

    public Gun getGun(String gunName) {
        for (Gun g : loadedGuns) {
            if (g.getGunName().toLowerCase().equals(gunName) || g.getFileName().toLowerCase().equals(gunName)) {
                return g;
            }
        }
        return null;
    }

    public ArrayList<Gun> getLoadedGuns() {
        ArrayList<Gun> ret = new ArrayList<>();
        for (Gun g : loadedGuns) {
            ret.add(g.copy());
        }
        return ret;
    }

    public boolean editLoadedGun(int typeId, Gun g) {
        boolean success = false;
        for (Gun gun : loadedGuns) {
            if (g.getGunMaterial().getId() == typeId) {
                loadedGuns.remove(gun);
                loadedGuns.add(g);
                success = true;
            }
        }
        return success;
    }

    public void removeBullet(Bullet bullet) {
        bullets.remove(bullet);
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public Bullet getBullet(Entity proj) {
        for (Bullet b : bullets) {
            if (b.getProjectile().getEntityId() == proj.getEntityId()) {
                return b;
            }
        }
        return null;
    }

    public void removeEffect(EffectType effectType) {
        this.effects.remove(effectType);
    }

    public void addEffect(EffectType effectType) {
        this.effects.add(effectType);
    }

    class UpdateTimer implements Runnable {
        public UpdateTimer() {
        }

        public void run() {
            GunPlayer.meta.forEach(GunPlayer::tick);
            for (Bullet t : bullets) {
                if (t != null) {
                    t.tick();
                }
            }
            for (EffectType eff : effects) {
                if (eff != null)
                    eff.tick();
            }
        }
    }

}