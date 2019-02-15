package com.festp;

public class DelayedTask {
	private int delay;
	private Runnable task;
	private boolean terminated = false;
	
	public DelayedTask(int start_delay, Runnable task)
	{
		this.delay = start_delay;
		this.task = task;
	}
	
	public boolean tick()
	{
		if (terminated)
			return false;
			
		delay--;
		if (delay > 0)
			return true;
		
		task.run();
		return false;
	}
	
	public void terminate()
	{
		terminated = true;
	}
	
	public void setDelay(int new_delay)
	{
		delay = new_delay;
	}
	
	public int getDelay()
	{
		return delay;
	}
}
