package com.xizz.scoreoflife.util;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.object.EventCheck;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Data {
	private static final String TAG = Data.class.getSimpleName();
	private static final int LIMIT = 1000;

	public static List<Event> getAllLocalEvents() throws com.parse.ParseException {
		ParseQuery<Event> query = ParseQuery.getQuery(Event.CLASS_NAME);
		query.fromLocalDatastore();
		query.orderByAscending(Event.ORDER_INDEX);
		return query.find();
	}

	public static List<EventCheck> getAllLocalEventChecks() throws com.parse.ParseException {
		return getLocalEventChecks(0, Long.MAX_VALUE);
	}

	public static List<EventCheck> getLocalEventChecks(long startDate, long endDate)
			throws com.parse.ParseException {
		ParseQuery<EventCheck> query = ParseQuery.getQuery(EventCheck.CLASS_NAME);
		query.fromLocalDatastore();
		query.orderByAscending(EventCheck.DATE);
		query.whereGreaterThanOrEqualTo(EventCheck.DATE, startDate);
		query.whereLessThanOrEqualTo(EventCheck.DATE, endDate);

		return query.find();
	}

	public static void deleteEvent(Event event) {
		// delete all checks relate to this event, then delete the event
		ParseQuery<EventCheck> query = ParseQuery.getQuery(EventCheck.CLASS_NAME);
		query.whereEqualTo(EventCheck.EVENT, event);
		query.fromLocalDatastore();
		try {
			List<EventCheck> checks = query.find();
			for (EventCheck check : checks) {
				check.deleteEventually();
			}
		} catch (ParseException e) {
			Log.e(TAG, "Error: " + e.getMessage());
		}
		event.deleteEventually();
	}

	public static void syncEvents() throws ParseException {
		ParseQuery<Event> query = ParseQuery.getQuery(Event.CLASS_NAME);
		query.setLimit(LIMIT);
		query.orderByAscending(Event.ORDER_INDEX);
		List<Event> events = query.find();

		ParseObject.unpinAll(Event.CLASS_NAME);
		ParseObject.pinAll(Event.CLASS_NAME, events);
	}

	public static void syncChecks() throws ParseException {
		List<EventCheck> checks = getAllCloudChecks();
		removeDuplicateChecks(checks);

		ParseObject.unpinAll(EventCheck.CLASS_NAME);
		ParseObject.pinAll(checks);
	}

	public static void updateOrderIndex() {
		try {
			List<Event> events = getAllLocalEvents();
			for (int i = 0; i < events.size(); ++i) {
				Event event = events.get(i);
				event.setOrderIndex(i + 1);
				event.saveEventually();
			}
		} catch (ParseException e) {
			Log.e(TAG, "Error reading local database: " + e.getMessage());
		}
	}

	public static void removeLegacyChecks(List<EventCheck> checks) throws ParseException {
		List<EventCheck> removeList = new LinkedList<>();
		for (EventCheck c : checks) {
			Event e = c.getEvent();
			if (e == null || c.getDate() < e.getStartDate() || c.getDate() > e.getEndDate())
				removeList.add(c);
		}
		for (EventCheck c : removeList) {
			checks.remove(c);
			c.deleteEventually();
		}
	}

	public static void removeDuplicateChecks(List<EventCheck> checks) throws ParseException {
		List<EventCheck> removeList = new LinkedList<>();
		for (int i = 0; i < checks.size() - 1; ++i) {
			for (int j = i + 1; j < checks.size(); ++j) {
				if (checks.get(i).getEvent() == checks.get(j).getEvent()
						&& checks.get(i).getDate() == checks.get(j).getDate()) {
					removeList.add(checks.get(i).getDone() ? checks.get(j) : checks.get(i));
				}
			}
		}
		for (EventCheck c : removeList) {
			checks.remove(c);
			c.deleteEventually();
		}
	}

	public static void createChecksIfNotExist(long date) throws ParseException {
		List<Event> events = getAllLocalEvents();
		List<EventCheck> checks = Data.getLocalEventChecks(date, date + Util.DAY_MILLI_SECS - 1);
		for (Event e : events) {
			if (date >= e.getStartDate() && date <= e.getEndDate() && !eventCheckExist(e,
					checks)) {
				EventCheck check = new EventCheck(e, date);
				check.pin();
				check.saveEventually();
			}
		}
	}

	public static long getEarliestDate() throws ParseException {
		List<Event> events = getAllLocalEvents();
		if (events == null || events.isEmpty())
			return Util.getToday();
		Event ealiestEvent = events.get(0);
		for (Event e : events) {
			if (e.getStartDate() < ealiestEvent.getStartDate())
				ealiestEvent = e;
		}

		return ealiestEvent.getStartDate();
	}

	private static boolean eventCheckExist(Event event, List<EventCheck> checks) {
		for (EventCheck c : checks) {
			if (event.equals(c.getEvent()))
				return true;
		}
		return false;
	}

	private static List<EventCheck> getAllCloudChecks() throws ParseException {
		ParseQuery<EventCheck> query = ParseQuery.getQuery(EventCheck.CLASS_NAME);
		query.setLimit(LIMIT);
		query.orderByAscending(EventCheck.DATE);
		List<EventCheck> checks = new ArrayList<>();
		for (int skip = 0; checks.size() == skip; ++skip) {
			query.setSkip(skip * LIMIT);
			checks.addAll(query.find());
		}
		return checks;
	}
}
