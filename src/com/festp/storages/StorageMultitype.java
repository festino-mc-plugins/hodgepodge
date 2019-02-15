package com.festp.storages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.DelayedTask;
import com.festp.Pair;
import com.festp.TaskList;
import com.festp.Utils;
import com.festp.storages.StorageMultitype.HandleTime;

public class StorageMultitype extends Storage
{
	public enum GrabDirection {FORWARD, BACKWARD}
	public enum SortMode {VANILLA, ALPHABET}
	public enum HandleTime {ON_BUTTON, WAIT_N_SECONDS, OPEN_CLOSE, ACTION}
	public enum InventoryAction {NOTHING, GAIN, GAIN_MERGING, LOSE} //org.bukkit.event.inventory.InventoryAction
	public enum UncraftMode {DENY, DROP}
	public static final GrabDirection DEFAULT_DIR = GrabDirection.BACKWARD;
	public static final SortMode DEFAULT_MODE = SortMode.VANILLA;
	public static final HandleTime DEFAULT_TIME = HandleTime.ON_BUTTON;
	public static final UncraftMode DEFAULT_UNCRAFT = UncraftMode.DENY;
	public static final int DELAY = 10 * 20; // 10s
	protected static final int FIRST_LEVEL_SIZE = 18, LEVEL_INCREASE = 18,
			MAX_LEVEL = 1 + (54 - FIRST_LEVEL_SIZE) / LEVEL_INCREASE;
	protected int level = -1;
	protected Inventory inventory;
	protected GrabDirection dir = DEFAULT_DIR;
	protected SortMode mode = DEFAULT_MODE;
	protected HandleTime sort_time = DEFAULT_TIME;
	protected HandleTime stack_time = DEFAULT_TIME;
	protected UncraftMode uncraft_mode = DEFAULT_UNCRAFT;
	protected boolean sorted = false, merged = false;
	protected DelayedTask task = null;
	public MultitypeMenu menu;

	public StorageMultitype(int ID, long full_time, int lvl)
	{
		super(ID, full_time);
		this.type = StorageType.MULTITYPE;
		
		if (lvl <= 0 || lvl > MAX_LEVEL)
			throw new IllegalArgumentException("Invalid argument \"lvl\"");
		level = lvl;
		inventory = pl.getServer().createInventory(null, FIRST_LEVEL_SIZE + LEVEL_INCREASE * (level - 1), "Storage");
		
		menu = new MultitypeMenu(this);
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
			if (stack_time == HandleTime.ACTION)
				instant = true;
			else if (stack_time == HandleTime.WAIT_N_SECONDS)
				delayed = true;
		}
		if (action == InventoryAction.GAIN || action == InventoryAction.LOSE)
		{
			sorted = false;
			if (sort_time == HandleTime.ACTION)
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
			Utils.drop(loc, stack, 1);
			inventory.setItem(i, null);
			i++;
		}
	}
	
	public int getLvl()
	{
		return level;
	}

	public void setGrabDirection(GrabDirection d) {
		dir = d;
	}
	
	public GrabDirection getGrabDirection() {
		return dir;
	}

	public void setSortMode(SortMode s) {
		mode = s;
	}
	
	public SortMode getSortMode() {
		return mode;
	}

	public void setSortTime(HandleTime s) {
		sort_time = s;
	}
	
	public HandleTime getSortTime() {
		return sort_time;
	}

	public void setStackTime(HandleTime s) {
		stack_time = s;
	}
	
	public HandleTime getStackTime() {
		return stack_time;
	}

	public void setUncraftMode(UncraftMode mode) {
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
		return false;
	}
	
	// on Item pickup
	/** @return grabbed amount */
	public int grabItemStack(ItemStack stack) {
		if(stack == null) return 0;
		int amount = stack.getAmount(), max_amount = stack.getMaxStackSize(), temp;
		ItemStack[] stacks = inventory.getContents();
		for (int i = 0; i < stacks.length; i++) {
			if (stack.isSimilar(stacks[i])) {
				temp = Math.min(amount, max_amount - stacks[i].getAmount());
				stacks[i].setAmount(stacks[i].getAmount() + temp);
			}
		}
		amount = stack.getAmount() - amount;
		stack.setAmount(stack.getAmount() - amount);
		return amount;
	}
	
	public Pair<Boolean, ItemStack[]> grabInventory(ItemStack[] inv) {
		if (inv == null) return new Pair<Boolean, ItemStack[]>(false, inv);
		boolean updated = false;
		ItemStack[] stacks = inventory.getContents();
		// split all incomplete stacks  into several classes
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
			inventory.setContents(stacks);
		
		return new Pair<Boolean, ItemStack[]>(updated, inv);
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
			if (stack_time == HandleTime.ACTION)
				mergeStacks();
			if (sort_time == HandleTime.ACTION)
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
		task = new DelayedTask(1, instant_task);
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
		return time == HandleTime.WAIT_N_SECONDS || time == HandleTime.ACTION || time == HandleTime.OPEN_CLOSE && isUnwatched();
	}
}
