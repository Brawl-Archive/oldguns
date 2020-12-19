package com.orange451.pvpgunplus;

import com.brawl.base.util.scheduler.Sync;
import com.orange451.pvpgunplus.commands.EnableTestingCommand;
import com.orange451.pvpgunplus.commands.TestingCommand;
import com.orange451.pvpgunplus.gun.*;
import com.orange451.pvpgunplus.listeners.PluginEntityListener;
import com.orange451.pvpgunplus.listeners.PluginEntityListener.PStat;
import com.orange451.pvpgunplus.listeners.PluginPlayerListener;
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

public class PVPGunPlus extends JavaPlugin
{
    private PluginPlayerListener playerListener = new PluginPlayerListener(this);
    private PluginEntityListener entityListener = new PluginEntityListener(this);
    public ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Gun> loadedGuns = new ArrayList<>();
    private ArrayList<EffectType> effects = new ArrayList<>();
    private ArrayList<GunPlayer> players = new ArrayList<>();
    private String pluginName = "PVPGunPlus";

    public int UpdateTimer;
    public Random random;

    public static PVPGunPlus plugin;

    public void onDisable()
    {
        System.out.println(this.pluginName + " disabled");
        clearMemory(true);
    }

    public void onEnable()
    {
        System.out.println(this.pluginName + " enabled");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.playerListener, this);
        pm.registerEvents(this.entityListener, this);
        new TestingCommand().registerCommand();
        new EnableTestingCommand().registerCommand();
        GunTests.init();

        startup(true);
       
        Sync.get().interval(1).run(() -> {
        	for(PStat stat : entityListener.stackedDamage.values()) {
        		if(stat.getStackedDamage().isEmpty())
        			continue;
        		
        		double damage = stat.getStackedDamage().poll();
                ((CraftLivingEntity)stat.getLent()).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, (float)damage);
        	}
        });
    }
    
    public static void resetPlayerDamage(LivingEntity entity, int ticks) {
    	CraftLivingEntity craftLiving = ((CraftLivingEntity)entity);
        EntityLiving minecraftLivingEntity = craftLiving.getHandle();
        
    	// Reset damage ticks directly. TEMP FIX //////////
        minecraftLivingEntity.maxNoDamageTicks =  ticks; //
        minecraftLivingEntity.noDamageTicks    =  ticks; //
        minecraftLivingEntity.hurtTicks        =  ticks; //
        minecraftLivingEntity.lastDamage       = -1;     //
    	///////////////////////////////////////////////////
        
        entity.setLastDamage(ticks);
        entity.setNoDamageTicks(ticks);
        entity.setLastDamageCause(null);
    }

    public void clearMemory(boolean init)
    {
        getServer().getScheduler().cancelTask(this.UpdateTimer);
        for (int i = bullets.size() - 1; i >= 0; i--)
        {
            bullets.get(i).destroy();
        }
        for (int i = players.size() - 1; i >= 0; i--)
        {
            players.get(i).unload();
        }
        if (init)
        {
            loadedGuns.clear();
        }
        bullets.clear();
        players.clear();
    }

    public void startup(boolean init)
    {
        this.UpdateTimer = getServer().getScheduler().scheduleSyncRepeatingTask(this, new UpdateTimer(), 20L, 1L);

        this.random = new Random();
        PVPGunPlus.plugin = this;

        File dir = new File(getPluginFolder());
        if (!dir.exists())
        {
            dir.mkdir();
        }

        File dir2 = new File(getPluginFolder() + "/guns");
        if (!dir2.exists())
        {
            dir2.mkdir();
        }

        dir2 = new File(getPluginFolder() + "/projectile");
        if (!dir2.exists())
        {
            dir2.mkdir();
        }

        if (init)
        {
            loadGuns();
            loadProjectile();
        }

        getOnlinePlayers();
    }

    private String getPluginFolder()
    {
        File file = getDataFolder();
        file.mkdir();
        return file.getAbsolutePath();
    }

    private void loadProjectile()
    {
        String path = getPluginFolder() + "/projectile";
        File dir = new File(path);
        String[] children = dir.list();
        if (children != null)
        {
            for (int i = 0; i < children.length; i++)
            {
                String filename = children[i];
                WeaponReader f = new WeaponReader(this, new File(path + "/" + filename), "gun");
                if (f.loaded)
                {
                    f.ret.node = ("pvpgunplus." + filename.toLowerCase());
                    this.loadedGuns.add(f.ret);
                    f.ret.setIsThrowable(true);
                    System.out.println("LOADED PROJECTILE: " + f.ret.getName());
                } else
                {
                    System.out.println("FAILED TO PROJECTILE GUN: " + f.ret.getName());
                }
            }
        }
    }

    private void loadGuns()
    {
        String path = getPluginFolder() + "/guns";
        File dir = new File(path);
        String[] children = dir.list();
        if (children != null)
        {
            for (int i = 0; i < children.length; i++)
            {
                String filename = children[i];
                WeaponReader f = new WeaponReader(this, new File(path + "/" + filename), "gun");
                if (f.loaded)
                {
                    f.ret.node = ("pvpgunplus." + filename.toLowerCase());
                    this.loadedGuns.add(f.ret);
                    System.out.println("LOADED GUN: " + f.ret.getName());
                } else
                {
                    System.out.println("FAILED TO LOAD GUN: " + f.ret.getName());
                }
            }
        }
    }

    public void reload(boolean b)
    {
        clearMemory(b);
        startup(b);
    }

    public void reload()
    {
        reload(false);
    }

    public static void playEffect(Effect e, Location l, int num)
    {
        for (Player player : Bukkit.getServer().getOnlinePlayers())
            player.playEffect(l, e, num);
    }

    public void getOnlinePlayers()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            GunPlayer g = new GunPlayer(this, player);
            this.players.add(g);
        }
    }

    class UpdateTimer implements Runnable
    {
        public UpdateTimer()
        {
        }

        public void run()
        {
            for (int i = players.size() - 1; i >= 0; i--)
            {
                GunPlayer gp = players.get(i);
                if (gp != null)
                {
                    gp.tick();
                }
            }
            for (int i = bullets.size() - 1; i >= 0; i--)
            {
                Bullet t = bullets.get(i);
                if (t != null)
                {
                    t.tick();
                }
            }
            for (int i = PVPGunPlus.this.effects.size() - 1; i >= 0; i--)
            {
                EffectType eff = (EffectType) PVPGunPlus.this.effects.get(i);
                if (eff != null)
                    eff.tick();
            }
        }
    }

    public static PVPGunPlus getPlugin()
    {
        return plugin;
    }

    public GunPlayer getGunPlayer(Player player)
    {
        for (int i = players.size() - 1; i >= 0; i--)
        {
            if (players.get(i).getPlayer().equals(player))
            {
                return players.get(i);
            }
        }
        return null;
    }
    
    public ArrayList<GunPlayer> getGunPlayers() {
    	return players;
    }

    public Gun getGun(int typeId)
    {
        for (int i = loadedGuns.size() - 1; i >= 0; i--)
        {
            if (loadedGuns.get(i).getGunMaterial() != null)
            {
                if (loadedGuns.get(i).getGunMaterial().getId() == typeId)
                {
                    return loadedGuns.get(i);
                }
            }
        }
        return null;
    }

    public Gun getGun(String gunName)
    {
        for (int i = loadedGuns.size() - 1; i >= 0; i--)
        {
            if (loadedGuns.get(i).getName().toLowerCase().equals(gunName) || loadedGuns.get(i).getFilename().toLowerCase().equals(gunName))
            {
                return loadedGuns.get(i);
            }
        }
        return null;
    }

    public void onJoin(Player player)
    {
        if (getGunPlayer(player) == null)
        {
            GunPlayer gp = new GunPlayer(this, player);
            players.add(gp);
        }
    }

    public void onQuit(Player player)
    {
        for (int i = players.size() - 1; i >= 0; i--)
        {
            if (players.get(i).getPlayer().getName().equals(player.getName()))
            {
                players.remove(i);
            }
        }
    }

    public ArrayList<Gun> getLoadedGuns()
    {
        ArrayList<Gun> ret = new ArrayList<Gun>();
        for (int i = loadedGuns.size() - 1; i >= 0; i--)
        {
            ret.add(loadedGuns.get(i).copy());
        }
        return ret;
    }
    
    public boolean editLoadedGun(int typeId, Gun g)
    {
    	boolean success = false;
        for (int i = loadedGuns.size() - 1; i >= 0; i--)
        {
            if (loadedGuns.get(i).getGunMaterial().getId() == typeId)
            {
               loadedGuns.set(i, g);
               success = true;
            }
        }
        return success;
    }

    public void removeBullet(Bullet bullet)
    {
        bullets.remove(bullet);
    }

    public void addBullet(Bullet bullet)
    {
        bullets.add(bullet);
    }

    public Bullet getBullet(Entity proj)
    {
        for (int i = bullets.size() - 1; i >= 0; i--)
        {
            if (bullets.get(i).getProjectile().getEntityId() == proj.getEntityId())
            {
                return bullets.get(i);
            }
        }
        return null;
    }

    public static Sound getSound(String gunSound)
    {
        String snd = gunSound.toUpperCase().replace(" ", "_");
        Sound sound = Sound.valueOf(snd);
        return sound;
    }

    public void removeEffect(EffectType effectType)
    {
        this.effects.remove(effectType);
    }

    public void addEffect(EffectType effectType)
    {
        this.effects.add(effectType);
    }

}