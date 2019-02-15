package com.festp;

import java.util.ArrayList;
import java.util.List;

public class TaskList {
	private static List<DelayedTask> tasks = new ArrayList<>();
	
	public static void add(DelayedTask task) {
		tasks.add(task);
	}
	
	public static void tick() {
		for (int i = tasks.size() - 1; i >= 0; i--)
			if (!tasks.get(i).tick())
				tasks.remove(i);
	}
	
	public static void removeAll()
	{
		tasks.clear();
	}
}
