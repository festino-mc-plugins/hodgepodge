package com.festp.storages;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.DelayedTask;
import com.festp.Pair;
import com.festp.TaskList;
import com.festp.storages.StorageMultitype.HandleTime;
import com.festp.utils.Utils;
import com.festp.utils.UtilsWorld;

public class StorageMultitype extends Storage
{
	public enum GrabDirection {FORWARD, BACKWARD}
	public enum GrabFilter {STACKING, SIMILAR, ANY}
	public enum SortMode {VANILLA, ALPHABET}
	public enum HandleTime {ON_BUTTON, WAIT_N_SECONDS, OPEN_CLOSE, ALWAYS}
	public enum InventoryAction {NOTHING, GAIN, GAIN_MERGING, LOSE}
	public enum UncraftMode {DENY, DROP}
	public static final GrabDirection DEFAULT_DIR = GrabDirection.BACKWARD;
	public static final GrabFilter DEFAULT_FILTER = GrabFilter.STACKING;
	public static final SortMode DEFAULT_MODE = SortMode.VANILLA;
	public static final HandleTime DEFAULT_TIME = HandleTime.ON_BUTTON;
	public static final UncraftMode DEFAULT_UNCRAFT = UncraftMode.DENY;
	public static final int DELAY = 5 * 20; // 5s
	public static final int FIRST_LEVEL_SIZE = 18, LEVEL_INCREASE = 18,
			MAX_LEVEL = 1 + (54 - FIRST_LEVEL_SIZE) / LEVEL_INCREASE;
	protected int level = -1;
	protected Inventory inventory;
	protected GrabDirection dir = DEFAULT_DIR;
	protected GrabFilter filter = DEFAULT_FILTER;
	protected SortMode mode = DEFAULT_MODE;
	protected HandleTime sort_time = DEFAULT_TIME;
	protected HandleTime stack_time = DEFAULT_TIME;
	protected UncraftMode uncraft_mode = DEFAULT_UNCRAFT;
	protected boolean sorted = false, merged = false;
	protected DelayedTask task = null;
	public MenuMultitype menu;

	public StorageMultitype(int ID, long full_time, int lvl)
	{
		super(ID, full_time);
		this.type = StorageType.MULTITYPE;
		
		if (lvl <= 0 || lvl > MAX_LEVEL)
			throw new IllegalArgumentException("Invalid argument \"lvl\"");
		level = lvl;
		inventory = Utils.getPlugin().getServer().createInventory(null, FIRST_LEVEL_SIZE + LEVEL_INCREASE * (level - 1), "Storage");
		
		menu = new MenuMultitype(this);
	}
	
	@Override
	public void setEdited(boolean new_val)
	{
		super.setEdited(new_val);
	}
	
	// action for Sort and Stack
	public void onAction(InventoryAction action)
	{
		if (action == InventoryAction.NOTHING)
			return;
		
		setEdited(true);
		boolean instant = false, delayed = false;
		if (action == InventoryAction.GAIN)
		{
			merged = false;
			if (stack_time == HandleTime.ALWAYS)
				instant = true;
			else if (stack_time == HandleTime.WAIT_N_SECONDS)
				delayed = true;
		}
		if (action == InventoryAction.GAIN || action == InventoryAction.LOSE || action == InventoryAction.GAIN_MERGING)
		{
			sorted = false;
			if (sort_time == HandleTime.ALWAYS)
				instant = true;
			else if (sort_time == HandleTime.WAIT_N_SECONDS)
				delayed = true;
		}
		if (instant)
			instantAction();
		if (delayed)
			delayAction();
	}

	public Inventory getMenu() {
		return menu.getMenu();
	}

	public Inventory getInventory()
	{
		return inventory;
	}
	
	public void setInventory(Inventory inv) {
		inventory = inv;
	}

	public boolean isAllowed(ItemStack item)
	{
		return Storage.getID(item) < 0;
	}
	
	public boolean isEmpty()
	{
		for(ItemStack stack : inventory.getContents())
			if(stack != null)
				return false;
		return true;
	}
	
	public void drop(Location loc)
	{
		setEdited(true);
		int i = 0;
		for(ItemStack stack : inventory.getContents()) {
			UtilsWorld.drop(loc, stack, 1);
			inventory.setItem(i, null);
			i++;
		}
	}
	
	public int getLvl()
	{
		return level;
	}

	public void setGrabFilter(GrabFilter f) {
		setEdited(true);
		filter = f;
	}
	
	public GrabFilter getGrabFilter() {
		return filter;
	}

	public void setGrabDirection(GrabDirection d) {
		setEdited(true);
		dir = d;
	}
	
	public GrabDirection getGrabDirection() {
		return dir;
	}

	public void setSortMode(SortMode s) {
		setEdited(true);
		mode = s;
	}
	
	public SortMode getSortMode() {
		return mode;
	}

	public void setSortTime(HandleTime s) {
		setEdited(true);
		sort_time = s;
	}
	
	public HandleTime getSortTime() {
		return sort_time;
	}

	public void setStackTime(HandleTime s) {
		setEdited(true);
		stack_time = s;
	}
	
	public HandleTime getStackTime() {
		return stack_time;
	}

	public void setUncraftMode(UncraftMode mode) {
		setEdited(true);
		uncraft_mode = mode;
	}
	
	public UncraftMode getUncraftMode() {
		return uncraft_mode;
	}

	public boolean canGrab(ItemStack stack)
	{
		if (stack == null)
			return false;
		int max_amount = stack.getMaxStackSize();
		ItemStack[] stacks = inventory.getContents();
		for (int i = 0 ; i < stacks.length; i++)
			if (stack.isSimilar(stacks[i]) && stacks[i].getAmount() < max_amount)
				return true;
		
		int empty = inventory.firstEmpty();
		if (empty < 0)
			return false;
		
		if (filter == GrabFilter.SIMILAR) {
			Material m = stack.getType();
			for (int i = 0 ; i < stacks.length; i++)
				if (stacks[i] != null && stacks[i].getType() == m)
					return true;
		}
		else if (filter == GrabFilter.ANY) {
			return true;
		}
		return false;
	}
	
	public void sort()
	{
		if (sorted)
			return;

		setEdited(true);
		switch (getSortMode()) {
		case ALPHABET:
			sort_Alphabet();
			break;
		case VANILLA:
			sort_Vanilla();
			break;
		}
		sorted = true;
	}

	public void sort_Vanilla()
	{
		sort_Vanilla(inventory);
	}

	public void sort_Alphabet()
	{
		sort_Alphabet(inventory);
	}
	
	public static void sort_Vanilla(Inventory inv)
	{
		ItemStack[] stacks = inv.getContents();
		ItemStack_SortV[] wrapped = new ItemStack_SortV[stacks.length];
		for (int i = 0; i < stacks.length; i++)
			wrapped[i] = new ItemStack_SortV(stacks[i]);
		Arrays.sort(wrapped);
		for (int i = 0; i < stacks.length; i++)
			stacks[i] = wrapped[i].getItemStack();
		inv.setContents(stacks);
	}
	
	public static void sort_Alphabet(Inventory inv)
	{
		ItemStack[] stacks = inv.getContents();
		ItemStack_SortA[] wrapped = new ItemStack_SortA[stacks.length];
		for (int i = 0; i < stacks.length; i++)
			wrapped[i] = new ItemStack_SortA(stacks[i]);
		Arrays.sort(wrapped);
		for (int i = 0; i < stacks.length; i++)
			stacks[i] = wrapped[i].getItemStack();
		inv.setContents(stacks);
	}

	public void mergeStacks()
	{
		if (merged)
			return;
		
		// replenish left stacks with right
		ItemStack[] stacks = inventory.getContents();
		for (int i = 0; i < stacks.length; i++)
		{
			if (stacks[i] == null)
				continue;
			int max = stacks[i].getMaxStackSize();
			if (stacks[i].getAmount() >= max)
				continue;
			for (int j = stacks.length - 1; j > i; j--)
				if (stacks[i].isSimilar(stacks[j]))
				{
					int lack = max - stacks[i].getAmount(), excess = stacks[j].getAmount();
					if (lack <= excess) {
						stacks[i].setAmount(max);
						stacks[j].setAmount(excess - lack);
						break;
					}
					else {
						stacks[i].setAmount(stacks[i].getAmount() + excess);
						stacks[j] = null;
					}
				}
		}
		inventory.setContents(stacks);
		merged = true;
		sorted = false; //can break the sort
	}

	private Runnable instant_task = new Runnable() {
		@Override
		public void run() {
			if (stack_time == HandleTime.ALWAYS)
				mergeStacks();
			if (sort_time == HandleTime.ALWAYS)
				sort();
		}
	};
	private Runnable delayed_task = new Runnable() {
		@Override
		public void run() {
			if (stack_time == HandleTime.WAIT_N_SECONDS)
				mergeStacks();
			if (sort_time == HandleTime.WAIT_N_SECONDS)
				sort();
			task = null;
		}
	};

	private void instantAction() {
		DelayedTask task = new DelayedTask(1, instant_task);
		TaskList.add(task);
	}
	private void delayAction() {
		if (task == null) {
			task = new DelayedTask(DELAY, delayed_task);
			TaskList.add(task);
		}
		else
			task.setDelay(DELAY);
	}
	
	public boolean isUnwatched()
	{
		return inventory.getViewers().size() == 0;
	}
	
	//if mode has been switched
	public boolean need_action(HandleTime time)
	{
		return time == HandleTime.WAIT_N_SECONDS || time == HandleTime.ALWAYS || time == HandleTime.OPEN_CLOSE && isUnwatched();
	}

	public int grabItemStack(ItemStack stack) {
		if(stack == null) return 0;
		if (!isAllowed(stack)) return 0;
		//also do not change stack
		int grabbed = grabItemStack_stacking(inventory, stack);
		if (grabbed != stack.getAmount())
			if (filter == GrabFilter.SIMILAR)
				grabbed += grabItemStack_similar(inventory, stack, stack.getAmount() - grabbed);
			else if (filter == GrabFilter.ANY)
				grabbed += grabItemStack_any(inventory, stack, stack.getAmount() - grabbed);
		return grabbed;
	}
	
	public static int grabItemStack_stacking(Inventory grabbing_inv, ItemStack stack)
	{
		int amount = stack.getAmount(), max_amount = stack.getMaxStackSize(), temp;
		ItemStack[] stacks = grabbing_inv.getContents();
		for (int i = 0; i < stacks.length; i++) {
			if (stack.isSimilar(stacks[i])) {
				temp = Math.min(amount, max_amount - stacks[i].getAmount());
				stacks[i].setAmount(stacks[i].getAmount() + temp);
				amount -= temp;
			}
		}
		amount = stack.getAmount() - amount;
		return amount;
	}
	
	public static int grabItemStack_similar(Inventory grabbing_inv, ItemStack stack, int remaining_amount)
	{
		
		int empty = grabbing_inv.firstEmpty();
		if (empty < 0)
			return 0;
		
		Material m = stack.getType();
		ItemStack[] stacks = grabbing_inv.getContents();
		boolean can_grab = false;
		for (int i = 0; i < stacks.length; i++)
			if (stacks[i] != null && stacks[i].getType() == m) {
				can_grab = true;
				break;
			}
		
		if (can_grab) {
			ItemStack copy = stack.clone();
			copy.setAmount(remaining_amount);
			grabbing_inv.setItem(empty, copy);
			return remaining_amount;
		}
		return 0;
	}
	
	public static int grabItemStack_any(Inventory grabbing_inv, ItemStack stack, int remaining_amount)
	{
		int empty = grabbing_inv.firstEmpty();
		if (empty < 0)
			return 0;

		ItemStack copy = stack.clone();
		copy.setAmount(remaining_amount);
		grabbing_inv.setItem(empty, copy);
		return remaining_amount;
	}

	public Pair<Boolean, ItemStack[]> grabInventory(ItemStack[] inv) {
		if(inv == null)
			return new Pair<Boolean, ItemStack[]>(false, null);
			
		Pair<Boolean, ItemStack[]> pair = grabInventory_stacking(inv);
		inv = pair.second;
		boolean updated = pair.first;
		
		if (filter == GrabFilter.SIMILAR)
			return grabInventory_similar(inv, updated);
		else if (filter == GrabFilter.ANY)
			return grabInventory_any(inv, updated);
		return pair;
	}
	private Pair<Boolean, ItemStack[]> grabInventory_stacking(ItemStack[] inv) {
		return grabInventory_stacking(inventory, inv, dir);
	}
	private Pair<Boolean, ItemStack[]> grabInventory_similar(ItemStack[] inv, boolean updated) {
		List<Material> materials = new ArrayList<>();
		ItemStack[] stacks = inventory.getContents();
		for (int i = 0; i < stacks.length; i++)
			if (stacks[i] != null)
				materials.add(stacks[i].getType());
		materials.sort(material_comp);
		Material[] types = materials.toArray(new Material[] {});
		int empty = 0;
		for (int i = 0; i < inv.length; i++)
			if (inv[i] != null && isAllowed(inv[i]))
				if (Arrays.binarySearch(types, inv[i].getType(), material_comp) >= 0)
				{
					while (empty < stacks.length && stacks[empty] != null)
						empty++;
					if (empty >= stacks.length)
						break;
					stacks[empty] = inv[i];
					inv[i] = null;
					updated = true;
				}
		if (updated)
			inventory.setContents(stacks);
		return new Pair<Boolean, ItemStack[]>(updated, inv);
	}
	private Pair<Boolean, ItemStack[]> grabInventory_any(ItemStack[] inv, boolean updated) {
		ItemStack[] stacks = inventory.getContents();
		for (int i = 0, empty = 0; i < inv.length; i++)
			if (inv[i] != null && isAllowed(inv[i])) {
				while (empty < stacks.length && stacks[empty] != null)
					empty++;
				if (empty < stacks.length) {
					stacks[empty] = inv[i];
					inv[i] = null;
					updated = true;
				}
				else {
					break;
				}
			}
		if (updated)
			inventory.setContents(stacks);
		return new Pair<Boolean, ItemStack[]>(updated, inv);
	}
	
	public static Pair<Boolean, ItemStack[]> grabInventory_stacking(Inventory grab_to, ItemStack[] inv, GrabDirection dir) {
		if (inv == null) return new Pair<Boolean, ItemStack[]>(false, inv);
		boolean updated = false;
		ItemStack[] stacks = grab_to.getContents();
		// split all incomplete stacks into several classes
		List<List<Integer>> entries = new ArrayList<>();
		for (int i = 0; i < stacks.length; i++) {
			if (stacks[i] == null || stacks[i].getAmount() == stacks[i].getMaxStackSize())
				continue;
			boolean is_new = true;
			for (int j = 0; j < entries.size(); j++)
				if (stacks[i].isSimilar(stacks[entries.get(j).get(0)])) {
					is_new = false;
					entries.get(j).add(i);
					break;
				}
			if (is_new) {
				List<Integer> l = new ArrayList<>();
				l.add(i);
				entries.add(l);
			}
		}
		
		// find overlaps with grabbing inventory
		@SuppressWarnings("unchecked")
		List<Integer>[] grabbing = new ArrayList[entries.size()];
		int start = -1, finish = -1, step = -1;
		if (dir == GrabDirection.FORWARD) {
			start = 0;
			finish = inv.length;
			step = 1;
		} else if (dir == GrabDirection.BACKWARD) {
			start = inv.length - 1;
			finish = -1;
			step = -1;
		}
		for (int i = 0; i < entries.size(); i++)
			grabbing[i] = new ArrayList<>();
		for (int i = start; i != finish; i+=step)
			if (inv[i] != null)
				for (int j = 0; j < entries.size(); j++)
					if (inv[i].isSimilar(stacks[entries.get(j).get(0)])) {
						grabbing[j].add(i);
						break;
					}
		
		//grabbing
		for (int i = 0; i < entries.size(); i++)
			if (grabbing[i].size() != 0) {
				int s = 0, e = 0, si, ei; // s - storage and e - external inventory indexes
				List<Integer> s_index = entries.get(i);
				List<Integer> e_index = grabbing[i];
				int max_amount = stacks[s_index.get(0)].getMaxStackSize();
				while (s < s_index.size() && e < e_index.size())
				{
					si = s_index.get(s);
					ei = e_index.get(e);
					int lack = max_amount - stacks[si].getAmount();
					int excess = inv[ei].getAmount();
					if (excess > lack) {
						inv[ei].setAmount(excess - lack);
						stacks[si].setAmount(max_amount);
						s++;
					}
					else {
						stacks[si].setAmount(stacks[si].getAmount() + excess);
						inv[ei] = null;
						e++;
					}
						
				}
				updated = true;
				break;
			}
		
		if (updated)
			grab_to.setContents(stacks);
		
		return new Pair<Boolean, ItemStack[]>(updated, inv);
	}

	private static Comparator<Material> material_comp = new Comparator<Material>() {
		public int compare(Material o1, Material o2) {
			return o1.compareTo(o2);
		}
	};

	/**Can grab any items, even Storages!*/
	public static Pair<Boolean, ItemStack[]> grabInventory_similar(Inventory grab_to, ItemStack[] inv, GrabDirection dir, boolean updated) {
		List<Material> materials = new ArrayList<>();
		ItemStack[] stacks = grab_to.getContents();
		for (int i = 0; i < stacks.length; i++)
			if (stacks[i] != null)
				materials.add(stacks[i].getType());
		materials.sort(material_comp);
		Material[] types = materials.toArray(new Material[] {});
		int empty = 0;
		for (int i = 0; i < inv.length; i++)
			if (inv[i] != null)
				if (Arrays.binarySearch(types, inv[i].getType(), material_comp) >= 0)
				{
					while (empty < stacks.length && stacks[empty] != null)
						empty++;
					if (empty >= stacks.length)
						break;
					stacks[empty] = inv[i];
					inv[i] = null;
					updated = true;
				}
		if (updated)
			grab_to.setContents(stacks);
		return new Pair<Boolean, ItemStack[]>(updated, inv);
	}

	/**Can grab any items, even Storages!*/
	public static Pair<Boolean, ItemStack[]> grabInventory_any(Inventory grab_to, ItemStack[] inv, GrabDirection dir, boolean updated) {
		ItemStack[] stacks = grab_to.getContents();
		for (int i = 0, empty = 0; i < inv.length; i++)
			if (inv[i] != null) {
				while (empty < stacks.length && stacks[empty] != null)
					empty++;
				if (empty < stacks.length) {
					stacks[empty] = inv[i];
					inv[i] = null;
					updated = true;
				}
				else {
					break;
				}
			}
		if (updated)
			grab_to.setContents(stacks);
		return new Pair<Boolean, ItemStack[]>(updated, inv);
	}
	
	@Override
	public String toString()
	{
		String location = "(unlocated)";
		if (getExternalInventory() != null) {
			Location l = getExternalInventory().getLocation();
			DecimalFormat f = new DecimalFormat("#0.00");
			location = "(" + f.format(l.getX()) + ";" + f.format(l.getY()) + ";" + f.format(l.getZ()) + ")";
		}
		return "StorageMultitype(ID=" + ID + ", location=" + location + ", level=" + getLvl() + ", "
				+ "Grab={grab_mode=" + canGrab() + ", grab_dir=" + getGrabDirection() + ", grab_filter=" + getGrabFilter() + "}, "
				+ "Sort={sort_mode=" + getSortMode() + ", sort_time=" + getSortTime() + "}, stack_time=" + getStackTime() + ", uncraft_mode=" + getUncraftMode() + ")";
	}
}
