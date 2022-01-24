package com.festp.tome;

/*import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;*/

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.ItemStack;

import com.festp.tome.SummonUtils.HorseSetter;
import com.festp.utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class HorseFormat {
	double max_health;
	double speed;
	double jump_strength;
	Class<? extends AbstractHorse> type;
	ItemStack[] inventory;
	boolean is_adult;
	
	// HORSE
	Color horse_color;
	Style horse_style;
	
	// CHESTED
	boolean chested_is_carrying;
	
	@Override
	public String toString()
	{
		JsonObject json = new JsonObject();
		json.addProperty("type", Utils.getShortBukkitClass(type));
		json.addProperty("max_health", max_health);
		json.addProperty("movement_speed", speed);
		json.addProperty("jump_strength", jump_strength);
		json.addProperty("is_adult", is_adult);
		json.addProperty("inventory", TomeFileManager.saveInventory(inventory));
		if (Horse.class.isAssignableFrom(type)) {
			json.addProperty("color", horse_color.name());
			json.addProperty("style", horse_style.name());
		} else if (ChestedHorse.class.isAssignableFrom(type)) {
			json.addProperty("is_chested", chested_is_carrying);
		}
		return json.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static HorseFormat fromString(String s)
	{
		if (s == null || s.equals("")) {
			return null;
		}
		
		JsonObject json;
		try {
			Object parsed = JsonParser.parseString(s);
			json = (JsonObject) parsed;
		} catch (JsonParseException e) {
			System.out.print("[] SummonerTome JSON parse error: " + s);
			e.printStackTrace();
			return null;
		}
		
		HorseFormat res = new HorseFormat();
		Class<?> resClass = Utils.getBukkitClass(json.get("type").getAsString());
		if (resClass == null) {
			System.out.print("[] SummonerTome horse class parse error: " + s);
			return null;
		}
		res.type = (Class<? extends AbstractHorse>) resClass;
		res.max_health = json.get("max_health").getAsDouble();
		res.speed = json.get("movement_speed").getAsDouble();
		res.jump_strength = json.get("jump_strength").getAsDouble();
		res.is_adult = json.get("is_adult").getAsBoolean();
		
		/*List<Map<String, Object>> inv = (List<Map<String, Object>>) json.get("inventory");
		res.inventory = new ItemStack[inv.size()];
		int i = 0;
		for (Map<String, Object> item : inv) {
			if (item != null)
				res.inventory[i] = ItemStack.deserialize(item);
			i++;
		}*/
		String inv = json.get("inventory").getAsString();
		res.inventory = TomeFileManager.loadInventory(inv);

		if (Horse.class.isAssignableFrom(res.type)) {
			res.horse_color = Color.valueOf(json.get("color").getAsString());
			res.horse_style = Style.valueOf(json.get("style").getAsString());
		} else if (ChestedHorse.class.isAssignableFrom(res.type)) {
			res.chested_is_carrying = json.get("is_chested").getAsBoolean();
		}

		return res;
	}
	
	public Class<? extends AbstractHorse> getHorseClass() {
		return type;
	}

	public void applyToHorse(AbstractHorse horse)
	{
		HorseSetter setter = new SummonUtils.HorseSetter() {
			@Override
			public void set(AbstractHorse new_horse) {
				new_horse.setTamed(true);
				new_horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(max_health);
				new_horse.setHealth(max_health);
				new_horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
				new_horse.setJumpStrength(jump_strength);
				if (is_adult) 	new_horse.setAdult();
				else 			new_horse.setBaby();
				
				if (new_horse instanceof Horse) {
					Horse h = (Horse) new_horse;
					h.setColor(horse_color);
					h.setStyle(horse_style);
				} else if (new_horse instanceof ChestedHorse) { // before inv filling
					ChestedHorse ch = (ChestedHorse) new_horse;
					ch.setCarryingChest(chested_is_carrying);
				}
				
				AbstractHorseInventory hinv = new_horse.getInventory();
				int i = 0;
				for (ItemStack is : inventory) {
					hinv.setItem(i, is);
					i++;
				}
			}
		};
		if (!type.isAssignableFrom(horse.getClass())) {
			Location loc = horse.getLocation();
			horse.remove();
			horse = SummonUtils.summonCustomHorse(loc, type, setter);
			horse.teleport(loc);
		} else {
			setter.set(horse);
		}
	}

	public static HorseFormat fromHorse(AbstractHorse horse)
	{
		HorseFormat res = new HorseFormat();
		res.type = horse.getClass();
		res.max_health = horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
		res.speed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
		res.jump_strength = horse.getAttribute(Attribute.HORSE_JUMP_STRENGTH).getBaseValue();
		res.is_adult = horse.isAdult();
		res.inventory = horse.getInventory().getContents();

		if (horse instanceof Horse) {
			Horse h = (Horse) horse;
			res.horse_color = h.getColor();
			res.horse_style = h.getStyle();
		} else if (horse instanceof ChestedHorse) {
			ChestedHorse ch = (ChestedHorse) horse;
			res.chested_is_carrying = ch.isCarryingChest();
		}

		return res;
	}
}
