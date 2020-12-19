package com.orange451.pvpgunplus.commands;

import com.brawl.Database;
import com.brawl.base.BrawlPlayer;
import com.brawl.base.command.RankOnlyCommand;
import com.brawl.database.minecraft.Tables;
import com.brawl.database.minecraft.tables.records.WarzTestsRecord;
import com.brawl.shared.Rank;
import com.brawl.shared.chat.C;
import com.brawl.shared.util.Duration;
import com.orange451.pvpgunplus.GunTests;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnableTestingCommand extends RankOnlyCommand {
    public EnableTestingCommand() {
        super(Rank.SMOD, "enableguntesting");
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        if(args.length != 2) {
            sender.sendMessage(C.cmdFail() + "Invalid usage: /enabletesting [testingkey] [duration]");
            return true;
        }
        if(!EnumUtils.isValidEnum(GunTests.class, args[0].toUpperCase())) {
            sender.sendMessage(C.cmdFail() + "Invalid testing key! Here's a list of possible testing keys");
            sender.sendMessage(C.info(C.RED) + StringUtils.join(Stream.of(GunTests.values()).map(t -> C.highlight(t.name())).collect(Collectors.toList()), ", "));
            return true;
        }
        GunTests test = GunTests.valueOf(args[0].toUpperCase());
        Duration length;
        if(NumberUtils.isNumber(args[1])) {
            length = Duration.days(30);
        }else {
            length = Duration.valueOf(args[1]);
            if(length.toMilliseconds() == 0) {
                sender.sendMessage(C.cmdFail() + "You used an invalid duration!");
                return true;
            }
        }
        WarzTestsRecord rec = Database.get().newRecord(Tables.WARZ_TESTS);
        rec.setKey("GUN_" + test.name());
        rec.setActivatedBy(BrawlPlayer.of(asPlayer(sender)) == null ? -1 : BrawlPlayer.of(asPlayer(sender)).getId());
        rec.setEndingTime(System.currentTimeMillis() + length.toMilliseconds());
        rec.insert();
        sender.sendMessage(C.cmdSuccess() + "Created testing period for test " + C.highlight(test.name()) + " for " + C.highlight(Duration.ms(rec.getEndingTime()).toString()) + "!");
        test.set(true, asPlayer(sender));
        sender.sendMessage(C.cmdSuccess() + "Activated the test!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if(args.length == 1) {
            return partialMatches(Stream.of(GunTests.values()).map(GunTests::name).collect(Collectors.toList()), args[0]);
        }else {
            return null;
        }
    }
}
