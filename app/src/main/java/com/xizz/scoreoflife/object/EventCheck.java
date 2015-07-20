package com.xizz.scoreoflife.object;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.sql.Date;

@ParseClassName("EventCheck")
public class EventCheck extends ParseObject {
	private final static String NEWLINE = System.getProperty("line.separator");

	public Event event;
	public long eventId;
	public long date;
	public boolean isDone;

	public EventCheck() {
		eventId = 0;
		date = 0;
		isDone = false;
	}

	public EventCheck(long id, long d) {
		eventId = id;
		date = d;
		isDone = false;
	}

	@Override
	public String toString() {
		if (event == null)
			return new Date(date) + NEWLINE + isDone + NEWLINE + eventId;
		else
			return new Date(date) + NEWLINE + isDone + NEWLINE + event;
	}
}