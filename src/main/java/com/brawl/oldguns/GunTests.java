//FLASH_TURNING("cs flashbangs (you can turn from them)"),
//WAR_MOLLY("Molotovs ported from war xd");
package com.brawl.oldguns;

import com.brawl.Database;
import com.brawl.database.warz.Tables;
import com.brawl.shared.chat.C;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.function.Function;

@Getter
public enum GunTests {
    FLASH_TURNING("cs flashbangs (you can turn from them)"),
    WAR_MOLLY("Molotovs ported from war xd");
    @Getter
    public static HashMap<GunTests, Boolean> tests = new HashMap<>();

    String desc;
    Function<CommandSender, Boolean> onEnable;
    Function<CommandSender, Boolean> onDisable;

    GunTests(String desc) {
        this.desc = desc;
        onEnable = null;
        onDisable = null;
    }

    GunTests(String desc, Function<CommandSender, Boolean> onEnable, Function<CommandSender, Boolean> onDisable) {
        this.desc = desc;
        this.onEnable = onEnable;
        this.onDisable = onDisable;
    }

    public static void init() {
        for (GunTests t : GunTests.values()) {
            tests.put(t, false);
        }
        Database.get().selectFrom(Tables.TESTS).fetch().stream().filter(g -> g.getKey().startsWith("GUN_")).forEach(rec -> {
            String key = rec.getKey().substring(4).toUpperCase();
            if (EnumUtils.isValidEnum(GunTests.class, key)) {
                GunTests test = GunTests.valueOf(key);
                if (System.currentTimeMillis() < rec.getEndingTime()) {
                    test.set(true, null);
                    System.out.println("Activating test " + rec.getKey() + " from database");
                } else {
                    System.out.println("Deleting test record " + rec.getKey());
                    Database.get().deleteFrom(Tables.TESTS).where(Tables.TESTS.KEY.eq(rec.getKey())).execute();
                }
            }
        });
    }

    public boolean isActive() {
        if (!tests.containsKey(this))
            return false;
        return tests.get(this);
    }

    public void toggle(CommandSender sender) {
        tests.put(this, !tests.containsKey(this) || !tests.get(this));
        if (isActive() && onEnable != null)
            if (onEnable.apply(sender)) {
                if (sender != null)
                    sender.sendMessage(C.cmdSuccess() + "Successfully activated Test " + C.highlight(name()) + "!");
            } else {
                if (sender != null)
                    sender.sendMessage(C.cmdFail() + "Couldn't activate test " + C.highlight(name()) + "!");
            }
        else if (onDisable != null)
            if (onDisable.apply(sender)) {
                if (sender != null)
                    sender.sendMessage(C.cmdSuccess() + "Successfully deactivated Test " + C.highlight(name()) + "!");
            } else {
                if (sender != null)
                    sender.sendMessage(C.cmdFail() + "Couldn't deactivate test " + C.highlight(name()) + "!");
            }
    }

    public void set(boolean b, CommandSender sender) {
        tests.put(this, b);
        if (b) {
            if (onEnable != null) {
                if (onEnable.apply(sender)) {
                    if (sender != null)
                        sender.sendMessage(C.cmdSuccess() + "Successfully activated Test " + C.highlight(name()) + "!");
                } else {
                    if (sender != null)
                        sender.sendMessage(C.cmdFail() + "Couldn't activate test " + C.highlight(name()) + "!");
                }
            }
        } else {
            if (onDisable != null) {
                if (onDisable.apply(sender)) {
                    if (sender != null)
                        sender.sendMessage(C.cmdSuccess() + "Successfully deactivated Test " + C.highlight(name()) + "!");
                } else {
                    if (sender != null)
                        sender.sendMessage(C.cmdFail() + "Couldn't deactivate test " + C.highlight(name()) + "!");
                }
            }
        }
    }

    @Override
    public String toString() {
        return (isActive() ? C.highlight("✔", C.GREEN) : C.highlight("✖", C.RED)) + " | " + C.highlight(name()) + " | " + C.highlight(getDesc());
    }

    @Getter
    @Builder
    public static class ActivateMessage {
        public String activatedBy;
        public GunTests test;
    }
}
