package com.haha01haha01.harail;

public class TrainEntry {
	public int id;
	public String text;

	public TrainEntry(int id, String text) {
		this.id = id;
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
