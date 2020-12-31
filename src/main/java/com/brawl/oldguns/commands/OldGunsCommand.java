package com.brawl.oldguns.commands;

import com.brawl.base.command.RankOnlyCommand;
import com.brawl.base.command.SubCommandExecutor;
import com.brawl.oldguns.commands.sub.EditCommand;
import com.brawl.oldguns.commands.sub.ListCommand;
import com.brawl.oldguns.commands.sub.ReloadCommand;
import com.brawl.oldguns.commands.sub.ToggleCommand;
import com.brawl.shared.Rank;
import com.brawl.shared.chat.C;
import org.bukkit.command.CommandSender;

public class OldGunsCommand extends RankOnlyCommand {
    public OldGunsCommand() {
        super(Rank.SMOD, "guns");

        SubCommandExecutor sce = new SubCommandExecutor() {
            @Override
            public boolean onExecuteDefault(CommandSender sender, String alias) {
                sender.sendMessage(C.DARK_GRAY + "----" + C.GRAY + "[" + C.YELLOW + "OldGuns" + C.GRAY + "]" + C.DARK_GRAY + "----");
                sender.sendMessage(C.GRAY + "/guns " + C.GREEN + "reload" + C.WHITE + " to reload the server");
                sender.sendMessage(C.GRAY + "/guns " + C.GREEN + "list" + C.WHITE + " to list the guns loaded into the server");
                sender.sendMessage(C.GRAY + "/guns " + C.GREEN + "toggle" + C.WHITE + " to toggle whether or not you can fire");
                sender.sendMessage(C.GRAY + "/guns " + C.GREEN + "edit [stat] [amount]" + C.WHITE + " to edit a gun in your hand");
                return true;
            }

            @Override
            public boolean onNoChildExecute(CommandSender sender, String alias, String[] args) {
                sender.sendMessage(C.cmdFail() + "Invalid Command " + C.highlight(args[0]) + "! Do /guns to see all commands, or tab complete /guns!");
                return true;
            }
        };
        sce.registerChildCommand(new ListCommand());
        sce.registerChildCommand(new ReloadCommand());
        sce.registerChildCommand(new ToggleCommand());
        sce.registerChildCommand(new EditCommand());

        setExecutor(sce);
    }

}
