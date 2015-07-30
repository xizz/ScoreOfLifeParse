package com.xizz.scoreoflife.object;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.sql.Date;

@ParseClassName("Event")
public class Event extends ParseObject implements Comparable<Event> {
	public static final String CLASS_NAME = Event.class.getSimpleName();
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String SCORE = "score";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String ORDER_INDEX = "orderIndex";

	private static final String NEWLINE = System.getProperty("line.separator");

	public Event() { }

	public Event(String name, int score, long startDate, long endDate, int orderIndex) {
		setName(name);
		setScore(score);
		setStartDate(startDate);
		setEndDate(endDate);
		setOrderIndex(orderIndex);
	}

	@Override
	public String toString() {
		return getName() + NEWLINE + "Score: " + getScore() + NEWLINE + "Start Date: "
				+ new Date(getStartDate());
	}

	public String getName() { return getString(NAME); }

	public void setName(String name) { put(NAME, name); }

	public int getScore() { return getInt(SCORE); }

	public void setScore(int score) { put(SCORE, score); }

	public long getStartDate() { return getLong(START_DATE); }

	public void setStartDate(long startDate) { put(START_DATE, startDate); }

	public long getEndDate() { return getLong(END_DATE); }

	public void setEndDate(long endDate) { put(END_DATE, endDate); }

	public int getOrderIndex() { return getInt(ORDER_INDEX); }

	public void setOrderIndex(int score) { put(ORDER_INDEX, score); }

	@Override
	public int compareTo(Event another) {
		return this.getOrderIndex() - another.getOrderIndex();
	}
}
