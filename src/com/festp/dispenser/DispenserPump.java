package com.festp.dispenser;

import org.bukkit.block.data.type.Dispenser;

public abstract class DispenserPump {
	Dispenser disp;
	
	public DispenserPump(Dispenser disp) {
		this.disp = disp;
	}
	
	public Dispenser getDispenser() {
		return disp;
	}
	
	public abstract boolean canPump();
	
	public abstract boolean pump();
}
