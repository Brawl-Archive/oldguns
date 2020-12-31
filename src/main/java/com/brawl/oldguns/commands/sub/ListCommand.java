package com.brawl.oldguns.commands.sub;

import com.brawl.base.command.RankOnlyCommand;
import com.brawl.oldguns.OldGuns;
import com.brawl.oldguns.gun.Gun;
import com.brawl.shared.Rank;
import com.brawl.shared.chat.C;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class ListCommand extends RankOnlyCommand {
    public ListCommand() {
        super(Rank.SMOD, "list");
        setUsage("/guns list");
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        sender.sendMessage(C.DGRAY + "------- " + C.highlight(C.YELLOW + "Currently Loaded Guns") + C.DGRAY + "-------");
        ArrayList<Gun> loadedGuns = OldGuns.getInstance().getLoadedGuns();
        for (Gun g : loadedGuns) {
            sender.sendMessage(" -" + g.getGunName() + C.YELLOW + "(" + g.getGunType() + ")" + C.GRAY + " AMMO: " + C.RED + g.getAmmoMaterial().toString() + C.GRAY + "  amt# " + C.RED + g.getAmmoAmtNeeded());
        }
        return true;
    }
}
