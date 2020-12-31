package com.brawl.oldguns.commands.sub;

import com.brawl.base.command.RankOnlyCommand;
import com.brawl.oldguns.OldGuns;
import com.brawl.oldguns.gun.GunPlayer;
import com.brawl.shared.Rank;
import com.brawl.shared.chat.C;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand extends RankOnlyCommand {
    public ToggleCommand() {
        super(Rank.SMOD, "toggle");
        setUsage("/guns toggle <user>");
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            GunPlayer gp = OldGuns.getInstance().getGunPlayer(asPlayer(sender));
            if (gp != null) {
                gp.setEnabled(!gp.isEnabled());
                String on = C.GREEN + "ON";
                String off = C.RED + "OFF";
                if (gp.isEnabled())
                    sender.sendMessage(C.cmdSuccess() + "You have turned your guns " + on);
                else
                    sender.sendMessage(C.cmdSuccess() + "You have turned your guns " + off);
            } else {
                sender.sendMessage(C.cmdSuccess() + "uh oh your gunplayer instance shouldn't be null");
            }
        } else if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[1]);

            if (target != null) {
                GunPlayer gp = OldGuns.getInstance().getGunPlayer(target);
                if (gp != null) {
                    gp.setEnabled(!gp.isEnabled());
                    String on = C.GREEN + "ON";
                    String off = C.RED + "OFF";
                    if (gp.isEnabled())
                        sender.sendMessage(C.cmdSuccess() + "You have turned " + C.highlight(target.getName()) + "'s guns " + on);
                    else
                        sender.sendMessage(C.cmdSuccess() + "You have turned " + C.highlight(target.getName()) + "'s guns " + off);
                }
            } else
                sender.sendMessage(C.cmdFail() + "Player not found!");
        } else {
            sender.sendMessage(C.cmdFail() + "Invalid Usage! " + getUsage());
        }
        return true;
    }
}
