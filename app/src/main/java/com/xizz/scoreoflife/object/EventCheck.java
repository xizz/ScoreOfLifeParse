package com.xizz.scoreoflife.object;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.sql.Date;

@ParseClassName("EventCheck")
public class EventCheck extends ParseObject implements Comparable<EventCheck> {
	public static final String CLASS_NAME = EventCheck.class.getSimpleName();
	public static final String EVENT = "event";
	public static final String DATE = "date";
	public static final String DONE = "done";

	private static final String NEWLINE = System.getProperty("line.separator");

	public EventCheck() { }

	public EventCheck(Event event, long date) {
		setEvent(event);
		setDate(date);
		setDone(false);
	}

	@Override
	public String toString() {
		return new Date(getDate()) + NEWLINE + getDone() + NEWLINE + getEvent();
	}

	public Event getEvent() { return (Event) getParseObject(EVENT); }

	public void setEvent(Event event) { put(EVENT, event);}

	public long getDate() { return getLong(DATE); }

	public void setDate(long date) { put(DATE, date);}

	public boolean getDone() { return getBoolean(DONE); }

	public void setDone(boolean done) { put(DONE, done);}

	@Override
	public int compareTo(EventCheck another) {
		if (this.getEvent() == null && another.getEvent() == null)
			return 0;
		else if (this.getEvent() == null)
			return -1;
		else if (another.getEvent() == null)
			return 1;
		else
			return this.getEvent().compareTo(another.getEvent());
	}
}