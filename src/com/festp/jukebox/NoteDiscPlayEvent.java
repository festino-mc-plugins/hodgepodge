package com.festp.jukebox;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NoteDiscPlayEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Location from;
    private final NoteSound sound;
    
    public NoteDiscPlayEvent(Location from, NoteSound note) {
    	this.from = from;
    	this.sound = note;
	}
    
    public Location getLocation() {
    	return from;
    }
    
    public NoteSound getNoteSound() {
    	return sound;
    }

	@Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
