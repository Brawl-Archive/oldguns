package com.brawl.oldguns.commands;

import com.brawl.Database;
import com.brawl.base.BrawlPlayer;
import com.brawl.base.BrawlPlugin;
import com.brawl.base.command.RankOnlyCommand;
import com.brawl.database.warz.Tables;
import com.brawl.database.warz.tables.records.TestsRecord;
import com.brawl.oldguns.GunTests;
import com.brawl.shared.Rank;
import com.brawl.shared.chat.C;
import com.brawl.shared.network.message.NetworkChannel;
import com.brawl.shared.network.message.NetworkMessage;
import com.brawl.shared.server.ServerType;
import com.brawl.shared.util.DBUtil;
import com.brawl.shared.util.Duration;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jooq.Result;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnableTestingCommand extends RankOnlyCommand {
    public EnableTestingCommand() {
        super(Rank.SMOD, "enableguntesting");
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(C.cmdFail() + "Invalid usage: /enabletesting [testingkey] [duration]");
            return true;
        }
        if (!EnumUtils.isValidEnum(GunTests.class, args[0].toUpperCase())) {
            sender.sendMessage(C.cmdFail() + "Invalid testing key! Here's a list of possible testing keys");
            sender.sendMessage(C.info(C.RED) + StringUtils.join(Stream.of(GunTests.values()).map(t -> C.highlight(t.name())).collect(Collectors.toList()), ", "));
            return true;
        }
        GunTests test = GunTests.valueOf(args[0].toUpperCase());
        Duration length;
        if (NumberUtils.isNumber(args[1])) {
            length = Duration.days(Integer.parseInt(args[1]));
        } else {
            length = Duration.valueOf(args[1]);
            if (length.toMilliseconds() == 0) {
                sender.sendMessage(C.cmdFail() + "You used an invalid duration!");
                return true;
            }
        }
        Result<TestsRecord> tests = Database.get().selectFrom(Tables.TESTS).where(Tables.TESTS.KEY.eq("GUN_" + test.name())).fetch();
        for (TestsRecord rec : tests) {
            rec.delete();
            sender.sendMessage(C.cmdFail() + "Overriding an old testing period activated by " + (rec.getActivatedBy() == -1 ? "console" : DBUtil.getPlayerName(rec.getActivatedBy())));
        }
        TestsRecord rec = Database.get().newRecord(Tables.TESTS);
        rec.setKey("GUN_" + test.name());
        rec.setActivatedBy(!(sender instanceof Player) || BrawlPlayer.of((Player) sender) == null ? -1 : BrawlPlayer.of(asPlayer(sender)).getId());
        rec.setEndingTime(System.currentTimeMillis() + length.toMilliseconds());
        rec.insert();
        sender.sendMessage(C.cmdSuccess() + "Created testing period for test " + C.highlight(test.name()) + " for " + C.highlight(length.toString()) + "!");
        GunTests.ActivateMessage message = GunTests.ActivateMessage.builder().test(test).activatedBy(rec.getActivatedBy() == -1 ? "Console" : DBUtil.getPlayerName(rec.getActivatedBy())).build();
        NetworkMessage nm = NetworkMessage.of(message).via(NetworkChannel.DATA).target(ServerType.WARZ, ServerType.TEST);
        BrawlPlugin.sendClientMessage(nm);
        sender.sendMessage(C.cmdSuccess() + "Activated the test on all servers!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return partialMatches(Stream.of(GunTests.values()).map(GunTests::name).collect(Collectors.toList()), args[0]);
        } else {
            return null;
        }
    }
}
