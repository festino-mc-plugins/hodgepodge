package com.festp.jukebox;

import java.util.List;

import com.festp.Pair;

public class NoteFormatException extends Exception {
	private static final long serialVersionUID = 1L;
	private final List<String> pages;
	private final Pair<Integer, Integer> from, to;
	
	public NoteFormatException(List<String> pages, Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
		super("Reason is unknown");
		this.pages = pages;
		this.from = from;
		this.to = to;
	}
	
	public NoteFormatException(List<String> pages, Pair<Integer, Integer> from, Pair<Integer, Integer> to, String reason) {
		super(reason);
		this.pages = pages;
		this.from = from;
		this.to = to;
	}
	
	public List<String> getPages() {
		return pages;
	}
	
	public Pair<Integer, Integer> getBegin() {
		return from;
	}
	
	public Pair<Integer, Integer> getEnd() {
		return to;
	}
}
