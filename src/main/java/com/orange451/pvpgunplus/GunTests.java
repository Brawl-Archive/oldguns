package com.orange451.pvpgunplus;

import com.brawl.shared.chat.C;
import lombok.Getter;

import java.util.HashMap;

@Getter
public enum GunTests {
    FLASH_TURNING("cs flashbangs (you can turn from them)"),
    WAR_MOLLY("Molotovs ported from war xd");
    @Getter
    public static HashMap<GunTests, Boolean> tests = new HashMap<>();

    String desc;
    Runnable onEnable;
    Runnable onDisable;

    GunTests() {
        desc = "";
        onEnable = null;
        onDisable = null;
    }
    GunTests(String desc) {
        this.desc = desc;
        onEnable = null;
        onDisable = null;
    }
    GunTests(String desc, Runnable onEnable, Runnable onDisable) {
        this.desc = desc;
        onEnable = null;
        onDisable = null;
    }
    public static void init() {
        for(GunTests t : GunTests.values()) {
            tests.put(t, false);
        }
    }
    public boolean isActive() {
        if(!tests.containsKey(this))
            return false;
        return tests.get(this);
    }
    public void toggle() {
        if(!tests.containsKey(this))
            tests.put(this, true);
        else tests.put(this, !tests.get(this));
    }
    public void set(boolean b) {
        tests.put(this, b);
    }

    @Override
    public String toString() {
        return (isActive() ? C.highlight("✔", C.GREEN) : C.highlight("✖", C.RED)) + " | " + C.highlight(name()) + " | " + C.highlight(getDesc());
    }
}
