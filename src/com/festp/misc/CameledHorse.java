package com.festp.misc;

import org.bukkit.entity.LivingEntity;

public class CameledHorse {
	LivingEntity camel;
	LivingEntity second_passenger;
	
	public CameledHorse(LivingEntity camel, LivingEntity second_passenger) {
		this.camel = camel;
		this.second_passenger = second_passenger;
	}
	
	public LivingEntity getCamel()
	{
		return camel;
	}
	
	public void setSecondPassenger(LivingEntity pass)
	{
		second_passenger = pass;
	}
}
