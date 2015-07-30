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
			e.printStackTrace();
		}
		event.deleteEventually();
	}

	public static void syncEvents() throws ParseException {
		ParseQuery<Event> query = ParseQuery.getQuery(Event.CLASS_NAME);
		query.setLimit(LIMIT);
		query.orderByAscending(Event.ORDER_INDEX);
		List<Event> events = query.find();

		ParseObject.unpinAll(Event.CLASS_NAME);
		Log.d(TAG, "Unppinned all old events from local database");

		ParseObject.pinAll(Event.CLASS_NAME, events);
		Log.d(TAG, "All new events pinned to local database");
	}

	public static void syncChecks() throws ParseException {
		List<EventCheck> checks = getAllCloudChecks();

		ParseObject.unpinAll(EventCheck.CLASS_NAME);
		Log.d(TAG, "Unppinned all old checks from local database");
		// TODO: Consider calling removeLegacyChecks()
		for (EventCheck c : checks) {
			Event e = c.getEvent();
			if (e == null || c.getDate() < e.getStartDate() || c.getDate() > e.getEndDate()) {
				c.deleteEventually();
				Log.d(TAG, "Deleting: " + c);
			} else {
				Log.d(TAG, "pinning: " + c);
				c.pin();
			}
		}
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
		// TODO: should also remove from database
		List<EventCheck> removeList = new LinkedList<>();

		List<Event> events = getAllLocalEvents();
		for (Event e : events) {
			for (EventCheck c : checks) {
				if (c.getEvent().equals(e)
						&& (c.getDate() < e.getStartDate() || c.getDate() > e.getEndDate())) {
					removeList.add(c);
				}
			}
		}
		for (EventCheck c : removeList) {
			checks.remove(c);
		}
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
