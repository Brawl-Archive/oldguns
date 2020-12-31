package com.brawl.oldguns.commands;

import com.brawl.base.command.RankOnlyCommand;
import com.brawl.oldguns.GunTests;
import com.brawl.shared.Rank;
import com.brawl.shared.chat.C;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestingCommand extends RankOnlyCommand {
    public TestingCommand() {
        super(Rank.MOD, "guntest");
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(C.warn(C.DGREEN) + "Welcome to Gun Testing! To see the status of a test, do /guntest info [key], and to toggle a key, just do /guntest [key]");
            sender.sendMessage(C.warn(C.DGREEN) + "Currently Active Keys (status | key | description)");
            for (GunTests entry : GunTests.getTests().keySet()) {
                sender.sendMessage(C.info(C.GREEN) + entry.toString());
            }
            return true;
        } else if (args.length == 1) {
            if (EnumUtils.isValidEnum(GunTests.class, args[0].toUpperCase())) {
                GunTests test = GunTests.valueOf(args[0].toUpperCase());
                test.toggle(sender);
                sender.sendMessage(C.warn(C.DGREEN) + "Toggled testing key " + C.highlight(test.name()) + "! Here is the updated key values (status | key | description)");
                sender.sendMessage(C.info(C.GREEN) + test.toString());
            } else {
                sender.sendMessage(C.cmdFail() + "Invalid Testing Key! Here's a list of possible testing keys: ");
                sender.sendMessage(C.info(C.RED) + StringUtils.join(Stream.of(GunTests.values()).map(t -> C.highlight(t.name())).collect(Collectors.toList()), ", "));
            }
            return true;
        } else if (args.length == 2) {
            if (!args[0].equalsIgnoreCase("info")) {
                sender.sendMessage(C.cmdFail() + "Invalid Usage!");
                sender.sendMessage(C.info(C.RED) + "To see the status of a test, do /guntest info [key], and to toggle a key, just do /guntest [key]");
            } else {
                if (EnumUtils.isValidEnum(GunTests.class, args[1].toUpperCase())) {
                    GunTests test = GunTests.valueOf(args[1].toUpperCase());
                    sender.sendMessage(C.highlight("> ", C.B + C.GREEN) + "Info for testing key " + C.highlight(test.name()) + "! (status | key | description)");
                    sender.sendMessage(C.info(C.GREEN) + test.toString());
                } else {
                    sender.sendMessage(C.cmdFail() + "Invalid Testing Key! Here's a list of possible testing keys: ");
                    sender.sendMessage(C.info(C.RED) + StringUtils.join(Stream.of(GunTests.values()).map(t -> C.highlight(t.name())).collect(Collectors.toList()), ", "));
                }
            }
            return true;
        } else {
            sender.sendMessage(C.cmdFail() + "Invalid Usage!");
            sender.sendMessage(C.info(C.RED) + "To see the status of a test, do /test info [key], and to toggle a key, just do /test [key]");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return partialMatches(Stream.of(GunTests.values()).map(GunTests::name).collect(Collectors.toList()), args[args.length - 1]);
        } else if (args.length == 2) {
            return partialMatches(Arrays.asList("true", "false"), args[args.length - 1]);
        }
        return null;
    }
}

