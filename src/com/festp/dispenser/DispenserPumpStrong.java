package com.festp.dispenser;

import org.bukkit.block.data.type.Dispenser;

public class DispenserPumpStrong extends DispenserPump {

	public DispenserPumpStrong(Dispenser disp) {
		super(disp);
	}

	public boolean canPump() {
		return false;
	}

	public boolean pump() {
		return false;
	}
}
