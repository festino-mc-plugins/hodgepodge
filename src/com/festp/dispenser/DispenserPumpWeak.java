package com.festp.dispenser;

import org.bukkit.block.data.type.Dispenser;

public class DispenserPumpWeak extends DispenserPump {

	public DispenserPumpWeak(Dispenser disp) {
		super(disp);
	}

	public boolean canPump() {
		return false;
	}

	public boolean pump() {
		return false;
	}
}
