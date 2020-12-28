package com.brawl.oldguns.events;

import com.brawl.base.event.BaseEvent;
import org.bukkit.event.Cancellable;

public class GunEvent extends BaseEvent implements Cancellable {

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.cancelled = arg0;
    }


}
