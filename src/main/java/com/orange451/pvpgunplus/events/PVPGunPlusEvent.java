package com.orange451.pvpgunplus.events;

import org.bukkit.event.Cancellable;

import com.brawl.base.event.BaseEvent;

public class PVPGunPlusEvent extends BaseEvent implements Cancellable
{

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
