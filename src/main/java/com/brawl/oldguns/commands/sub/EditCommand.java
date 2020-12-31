package com.brawl.oldguns.commands.sub;

import com.brawl.base.command.RankOnlyCommand;
import com.brawl.oldguns.OldGuns;
import com.brawl.oldguns.gun.Gun;
import com.brawl.shared.Rank;
import com.brawl.shared.chat.C;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditCommand extends RankOnlyCommand {
    public EditCommand() {
        super(Rank.SMOD, "edit");
        setUsage("/guns edit [property] [value]");
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        Player player = asPlayer(sender);
        int id = player.getItemInHand().getTypeId();
        Gun g = OldGuns.getInstance().getGun(id);

        player.sendMessage(C.cmdSuccess() + "Editing gun: " + g.getGunName());

        switch (args[0]) {
            case "accuracy":
                g.setAccuracy(Double.parseDouble(args[1]));
                break;
            case "accuracyAimed":
                g.setAccuracy_aimed(Double.parseDouble(args[1]));
                break;
            case "accuracyCrouched":
                g.setAccuracy_crouched(Double.parseDouble(args[1]));
                break;
            case "armorPenetration":
                g.setArmorPenetration(Integer.parseInt(args[1]));
                break;
            case "bulletSpeed":
                g.setBulletSpeed(Double.parseDouble(args[1]));
                break;
            case "bulletsPerClick":
                g.setBulletsPerClick(Integer.parseInt(args[1]));
                break;
            case "gunDamage":
                g.setGunDamage(Integer.parseInt(args[1]));
                break;
            case "gunType":
                g.setGunType(args[1]);
                break;
            case "knockback":
                g.setKnockback(Double.parseDouble(args[1]));
                break;
            case "recoil":
                g.setRecoil(Double.parseDouble(args[1]));
                break;
            case "reloadTime":
                g.setReloadTime(Integer.parseInt(args[1]));
                break;
            case "roundsPerBurst":
                g.setRoundsPerBurst(Integer.parseInt(args[1]));
                break;
            case "maxDistance":
                g.setMaxDistance(Integer.parseInt(args[1]));
                break;
            case "bulletDelayTime":
                g.setBulletDelayTime(Integer.parseInt(args[1]));
                break;
            default:
                sender.sendMessage(C.cmdFail() + "Stat not found, must be: accuracy, accuracyAimed, accuracyCrouched, "
                        + "armorPenetration, bulletSpeed, bulletsPerClick, gunDamage, gunType, knockback, recoil, reloadTime, "
                        + "roundsPerBurst, maxDistance, bulletDelayTime");
        }
        OldGuns.getInstance().editLoadedGun(id, g);
        //plugin.editLoadedGun(id, g);

        sender.sendMessage(C.cmdSuccess() + "Edited gun!");
        return true;
    }
}
