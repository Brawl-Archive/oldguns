package com.brawl.oldguns.gun;

import com.brawl.oldguns.OldGuns;
import org.bukkit.Effect;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class WeaponReader {
    public OldGuns plugin;
    public boolean loaded = false;
    public File file;
    public String weaponType;
    public Gun ret;

    public WeaponReader(OldGuns plugin, File file, String string) {
        this.plugin = plugin;
        this.file = file;
        this.weaponType = string;
        this.ret = new Gun(file.getName());
        ret.setFileName(file.getName().toLowerCase());
        this.load();
    }


    private void computeData(String str) {
        try {
            if (str.indexOf("=") > 0) {
                String var = str.substring(0, str.indexOf("=")).toLowerCase();
                String val = str.substring(str.indexOf("=") + 1);
                if (var.equals("gunname"))
                    ret.setName(val);
                if (var.equals("guntype"))
                    ret.setGunType(val);
                if (var.equals("ammoamtneeded"))
                    ret.setAmmoAmtNeeded(Integer.parseInt(val));
                if (var.equals("reloadtime"))
                    ret.setReloadTime(Integer.parseInt(val));
                if (var.equals("gundamage"))
                    ret.setGunDamage(Integer.parseInt(val));
                if (var.equals("armorpenetration"))
                    ret.setArmorPenetration(Integer.parseInt(val));
                if (var.equals("ammotype"))
                    ret.setAmmoType(val);
                if (var.equals("roundsperburst"))
                    ret.setRoundsPerBurst(Integer.parseInt(val));
                if (var.equals("maxdistance"))
                    ret.setMaxDistance(Integer.parseInt(val));
                if (var.equals("bulletsperclick"))
                    ret.setBulletsPerClick(Integer.parseInt(val));
                if (var.equals("bulletspeed"))
                    ret.setBulletSpeed(Double.parseDouble(val));
                if (var.equals("accuracy"))
                    ret.setAccuracy(Double.parseDouble(val));
                if (var.equals("accuracy_aimed"))
                    ret.setAccuracy_aimed(Double.parseDouble(val));
                if (var.equals("accuracy_crouched"))
                    ret.setAccuracy_crouched(Double.parseDouble(val));
                if (var.equals("exploderadius"))
                    ret.setExplodeRadius(Double.parseDouble(val));
                if (var.equals("gunvolume"))
                    ret.setGunVolume(Double.parseDouble(val));
                if (var.equals("fireradius"))
                    ret.setFireRadius(Double.parseDouble(val));
                if (var.equals("flashradius"))
                    ret.setFlashRadius(Double.parseDouble(val));
                if (var.equals("canheadshot"))
                    ret.setCanHeadshot(Boolean.parseBoolean(val));

                if (var.equals("canshootleft"))
                    ret.setCanClickLeft(Boolean.parseBoolean(val));
                if (var.equals("canshootright"))
                    ret.setCanClickRight(Boolean.parseBoolean(val));
                if (var.equals("canclickleft"))
                    ret.setCanClickLeft(Boolean.parseBoolean(val));
                if (var.equals("canclickright"))
                    ret.setCanClickRight(Boolean.parseBoolean(val));

                if (var.equals("destroybulletwhenhit"))
                    ret.setDestroyBulletWhenHit(Boolean.parseBoolean(val));

                if (var.equals("knockback"))
                    ret.setKnockback(Double.parseDouble(val));
                if (var.equals("recoil"))
                    ret.setRecoil(Double.parseDouble(val));
                if (var.equals("canaim"))
                    ret.setCanAimLeft(Boolean.parseBoolean(val));
                if (var.equals("canaimleft"))
                    ret.setCanAimLeft(Boolean.parseBoolean(val));
                if (var.equals("canaimright"))
                    ret.setCanAimRight(Boolean.parseBoolean(val));
                if (var.equals("outofammomessage"))
                    ret.setOutOfAmmoMessage(val);
                if (var.equals("permissionmessage"))
                    ret.setPermissionMessage(val);
                if (var.equals("bullettype"))
                    ret.setProjType(val);
                if (var.equals("needspermission"))
                    ret.setNeedsPermission(Boolean.parseBoolean(val));
                if (var.equals("hassmoketrail"))
                    ret.setHasSmokeTrail(Boolean.parseBoolean(val));
                if (var.equals("gunsound"))
                    ret.addGunSounds(val);
                if (var.equals("maxclipsize"))
                    ret.setMaxClipSize(Integer.parseInt(val));
                if (var.equals("hasclip"))
                    ret.setHasClip(Boolean.parseBoolean(val));
                if (var.equals("reloadgunondrop"))
                    ret.setReloadGunOnDrop(Boolean.parseBoolean(val));
                if (var.equals("localgunsound"))
                    ret.setLocalGunSound(Boolean.parseBoolean(val));
                if (var.equalsIgnoreCase("canGoPastMaxDistance"))
                    ret.setCanGoPastMaxDistance(Boolean.parseBoolean(val));
                if (var.equalsIgnoreCase("ignoreitemdata"))
                    ret.setIgnoreItemData(Boolean.parseBoolean(val));
                if (var.equals("bulletdelaytime"))
                    ret.setBulletDelayTime(Integer.parseInt(val));
                if (var.equals("explosiondamage"))
                    ret.setExplosionDamage(Integer.parseInt(val));
                if (var.equals("timeuntilrelease"))
                    ret.setReleaseTime(Integer.parseInt(val));
                if (var.equals("reloadtype"))
                    ret.setReloadType(val);
                if (var.equals("play_effect_on_release")) {
                    String[] effDat = val.split(",");
                    if (effDat.length == 3) {
                        double radius = Double.parseDouble(effDat[0]);
                        int duration = Integer.parseInt(effDat[1]);
                        Effect eff = Effect.valueOf(effDat[2].toUpperCase());
                        this.ret.setReleaseEffect(new EffectType(duration, radius, eff));
                    } else if (effDat.length == 4) {
                        double radius = Double.parseDouble(effDat[0]);
                        int duration = Integer.parseInt(effDat[1]);
                        Effect eff = Effect.valueOf(effDat[2].toUpperCase());
                        byte specialDat = Byte.parseByte(effDat[3]);
                        EffectType effect = new EffectType(duration, radius, eff);
                        effect.setSpecialDat(specialDat);
                        this.ret.setReleaseEffect(effect);
                    }
                }
            }
        } catch (Exception e) {
            this.loaded = false;
        }
    }

    public void load() {
        loaded = true;
        ArrayList<String> file = new ArrayList<>();
        try {
            FileInputStream fstream = new FileInputStream(this.file.getAbsolutePath());
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                file.add(strLine);
            }
            br.close();
            in.close();
            fstream.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        for (String s : file) {
            computeData(s);
        }
    }
}
