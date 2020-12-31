package com.brawl.oldguns.commands.sub;

import com.brawl.base.command.RankOnlyCommand;
import com.brawl.oldguns.OldGuns;
import com.brawl.shared.Rank;
import com.brawl.shared.chat.C;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends RankOnlyCommand {
    public ReloadCommand() {
        super(Rank.SMOD, "reload");
        setUsage("/guns reload");
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        OldGuns.getInstance().reload(true);
        sender.sendMessage(C.cmdSuccess() + "Reloaded the guns plugin!");
        return true;
    }
}
