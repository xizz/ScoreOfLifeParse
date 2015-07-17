package com.xizz.scoreoflife.object;

import java.sql.Date;

public class Event {
	public long id;
	public String name;
	public int score;
	public long startDate;
	public long endDate;
	public int orderIndex;
	private String NEWLINE = System.getProperty("line.separator");

	public Event() {
		name = "";
		score = 0;
		startDate = 0;
		endDate = Long.MAX_VALUE;
		orderIndex = 0;
	}

	public Event(String n, int s, long sd, int i, long ed) {
		name = n;
		score = s;
		startDate = sd;
		orderIndex = i;
		endDate = ed;
	}

	@Override
	public String toString() {
		return name + NEWLINE + "Score: " + score + NEWLINE + "Start Date: "
				+ new Date(startDate);
	}
}
