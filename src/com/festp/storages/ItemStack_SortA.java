package com.festp.storages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.Utils;

public class ItemStack_SortA implements Comparable {
	private ItemStack stack;
	private boolean default_name;
	private Ench[] ench_names;
	
	public ItemStack_SortA(ItemStack is)
	{
		stack = is;
		if (stack != null)
		{
			default_name = Utils.isRenamed(is);
			Map<Enchantment, Integer> enchs = getEnchantments(stack);
			ench_names = new Ench[enchs.size()];
			int i = 0;
			for (Entry<Enchantment, Integer> entry : enchs.entrySet())
			{
				ench_names[i] = new Ench(entry.getKey(), entry.getValue());
				i++;
			}
			Arrays.sort(ench_names);
		}
	}
	
	public ItemStack getItemStack() {
		return stack;
	}
	
	@Override
	public int compareTo(Object ob) {
		if (!(ob instanceof ItemStack_SortA))
			return 0;
		ItemStack_SortA is = (ItemStack_SortA)ob;
		ItemStack stack2 = is.stack;
		if (stack == null || stack2 == null)
			return comp_true(stack == null, stack2 == null);
		
		int material_comp = comp(stack.getType().toString(), stack2.getType().toString());
		if (material_comp != 0)
			return material_comp;
		
		int def_comp = comp_true(default_name, is.default_name);
		if (def_comp != 0)
			return def_comp;
		
		if (!default_name)
		{
			int name_comp = comp(stack.getItemMeta().getDisplayName(), stack2.getItemMeta().getDisplayName());
			if (name_comp != 0)
				return name_comp;
		}
		
		if (stack.hasItemMeta() && stack2.hasItemMeta())
		{
			int size_comp = comp(ench_names.length, is.ench_names.length);
			if (size_comp != 0)
				return size_comp;
			
			int size = ench_names.length;
			for (int i = 0; i < size; i++) {
				int name_comp = ench_names[i].name.compareTo(is.ench_names[i].name);
				if (name_comp != 0)
					return name_comp;
				int lvl_comp = comp(ench_names[i].level, is.ench_names[i].level);
				if (lvl_comp != 0)
					return lvl_comp;
			}
		}
		
		return 0;
	}
	
	public static Map<Enchantment, Integer> getEnchantments(ItemStack s)
	{
		Map<Enchantment, Integer> enchs = new HashMap<Enchantment, Integer>();
		if (s == null || !s.hasItemMeta())
			return enchs;
		ItemMeta im = s.getItemMeta();
		enchs.putAll(im.getEnchants());
		if (im instanceof EnchantmentStorageMeta)
			enchs.putAll(((EnchantmentStorageMeta)im).getStoredEnchants());
		return enchs;
	}
	
	private int comp(int a, int b)
	{
		if (a > b)
			return 1;
		if (a < b)
			return -1;
		return 0;
	}
	private int comp(String a, String b)
	{
		return a.compareTo(b);
	}
	private int comp_true(boolean a, boolean b)
	{
		if (a && !b)
			return 1;
		if (!a && b)
			return -1;
		return 0;
	}
	
	private class Ench implements Comparable {
		String name;
		int level;
		
		public Ench(Enchantment ench, int lvl) {
			name = ench.toString();
			level = lvl;
		}
		
		@Override
		public int compareTo(Object o) {
			if (!(o instanceof String))
				return 0;
			return name.compareTo((String)o);
		}
	}
}
