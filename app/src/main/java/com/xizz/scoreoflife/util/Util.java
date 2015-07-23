package com.xizz.scoreoflife.util;

import com.parse.ParseQuery;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.object.EventCheck;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Util {

	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.ENGLISH);
	public final static long ONEDAY = 86400000;

	public final static String NEWLINE = System.getProperty("line.separator");

	public final static int REQUEST_ADD = 111;
	public final static int REQUEST_EDIT = 222;


	public static void removeLegacyChecks(List<Event> events,
	                                      List<EventCheck> checks) {
		List<EventCheck> removeList = new LinkedList<>();
		for (Event e : events) {
			for (EventCheck c : checks) {
				if (c.getEvent().getObjectId().equals(e.getObjectId())
						&& (c.getDate() < e.getStartDate() || c.getDate() > e.getEndDate())) {
					removeList.add(c);
				}
			}
		}
		for (EventCheck c : removeList) {
			checks.remove(c);
		}
	}

	public static long getToday() {
		try {
			return DATE_FORMAT.parse(DATE_FORMAT.format(new Date())).getTime();
		} catch (ParseException e) {
			return 0;
		}
	}

	public static List<EventCheck> getEventChecks(long startDate, long endDate)
			throws com.parse.ParseException {
		// TODO: Order these checks by orderindex
		ParseQuery<EventCheck> query = ParseQuery.getQuery(EventCheck.CLASS_NAME);
		query.fromLocalDatastore();
		query.whereGreaterThanOrEqualTo(EventCheck.DATE, startDate);
		query.whereLessThanOrEqualTo(EventCheck.DATE, endDate);
		return query.find();
	}

	public static List<Event> getAllEvents() throws com.parse.ParseException {
		ParseQuery<Event> query = ParseQuery.getQuery(Event.CLASS_NAME);
		query.fromLocalDatastore();
		query.orderByAscending(Event.ORDER_INDEX);
		return query.find();

	}
}
