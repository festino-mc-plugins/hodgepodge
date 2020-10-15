package com.festp.jukebox;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import com.festp.utils.ITickable;

public class RecordingBookList implements ITickable {

	private final List<RecordingBook> books = new ArrayList<>();
	
	@Override
	public void tick() {
		for (int i = books.size() - 1; i >= 0; i--) {
			RecordingBook book = books.get(i);
			if (book.canTick()) {
				book.finishTick();
			} else {
				remove(i);
			}
		}
	}
	
	public void add(Player player, EquipmentSlot slot) {
		remove(player);
		books.add(new RecordingBook(player, slot));
	}
	
	public void remove(Player player) {
		for (int i = books.size() - 1; i >= 0; i--) {
			RecordingBook book = books.get(i);
			if (book.player.equals(player)) {
				remove(i);
				break;
			}
		}
	}
	
	private void remove(int i) {
		RecordingBook book = books.get(i);
		book.reduceSize();
		books.remove(i);
		book.player.closeInventory();
	}

	public void play(int instId, int nbsSemitone, Location from) {
		for (RecordingBook book : books) {
			Player player = book.player;
			if (player.getWorld() == from.getWorld() && player.getLocation().distance(from) <= NoteUtils.SOUND_DISTANCE) {
				book.appendToTick(instId, nbsSemitone);
			}
		}
	}
}
