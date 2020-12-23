package com.orange451.pvpgunplus.commands;

import com.brawl.*;
import com.brawl.base.*;
import com.brawl.base.command.*;
import com.brawl.database.warz.Tables;
import com.brawl.database.warz.tables.records.TestsRecord;
import com.brawl.shared.*;
import com.brawl.shared.chat.*;
import com.brawl.shared.network.message.*;
import com.brawl.shared.server.*;
import com.brawl.shared.util.*;
import com.orange451.pvpgunplus.*;
import org.apache.commons.lang.math.*;
import org.apache.commons.lang3.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.jooq.*;

import java.util.*;
import java.util.stream.*;

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
