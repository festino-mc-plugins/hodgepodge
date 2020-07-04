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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.festp.tome.SummonUtils.HorseSetter;
import com.festp.utils.Utils;

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
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString()
	{
		JSONObject json = new JSONObject();
		json.put("type", Utils.getShortBukkitClass(type));
		json.put("max_health", max_health);
		json.put("movement_speed", speed);
		json.put("jump_strength", jump_strength);
		json.put("is_adult", is_adult);
		/*List<Object> inv = new ArrayList<>();
		for (ItemStack item : inventory)
			if (item != null) {
				HashMap<String, Object> map = new HashMap<>();
				for (Entry<String, Object> entry : item.serialize().entrySet()) {
					if (entry.getKey().equalsIgnoreCase("meta")) {
						map.put(entry.getKey(), entry.getValue().toString());
					} else {
						map.put(entry.getKey(), entry.getValue());
					}
				}
				inv.add(map);
				//inv.add(item.serialize());
			} else
				inv.add(null);
		json.put("inventory", inv);*/
		json.put("inventory", TomeFileManager.saveInventory(inventory));
		if (Horse.class.isAssignableFrom(type)) {
			json.put("color", horse_color.name());
			json.put("style", horse_style.name());
		} else if (ChestedHorse.class.isAssignableFrom(type)) {
			json.put("is_chested", chested_is_carrying);
		}
		return json.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	public static HorseFormat fromString(String s)
	{
		if (s == null || s.equals("")) {
			return null;
		}
		
		JSONParser parser = new JSONParser();
		JSONObject json;
		try {
			Object parsed = parser.parse(s);
			json = (JSONObject) parsed;
		} catch (ParseException e) {
			System.out.print("[] SummonerTome JSON parse error: " + s);
			e.printStackTrace();
			return null;
		}
		
		HorseFormat res = new HorseFormat();
		Class<?> resClass = Utils.getBukkitClass((String) json.get("type"));
		if (resClass == null) {
			System.out.print("[] SummonerTome horse class parse error: " + s);
			return null;
		}
		res.type = (Class<? extends AbstractHorse>) resClass;
		res.max_health = (double) json.get("max_health");
		res.speed = (double) json.get("movement_speed");
		res.jump_strength = (double) json.get("jump_strength");
		res.is_adult = (boolean) json.get("is_adult");
		
		/*List<Map<String, Object>> inv = (List<Map<String, Object>>) json.get("inventory");
		res.inventory = new ItemStack[inv.size()];
		int i = 0;
		for (Map<String, Object> item : inv) {
			if (item != null)
				res.inventory[i] = ItemStack.deserialize(item);
			i++;
		}*/
		String inv = (String) json.get("inventory");
		res.inventory = TomeFileManager.loadInventory(inv);

		if (Horse.class.isAssignableFrom(res.type)) {
			res.horse_color = Color.valueOf((String) json.get("color"));
			res.horse_style = Style.valueOf((String) json.get("style"));
		} else if (ChestedHorse.class.isAssignableFrom(res.type)) {
			res.chested_is_carrying = (boolean) json.get("is_chested");
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
