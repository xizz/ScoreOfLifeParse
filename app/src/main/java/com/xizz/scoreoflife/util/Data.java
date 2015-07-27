package com.xizz.scoreoflife.util;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.object.EventCheck;

import java.util.List;

public class Data {
	private static String TAG = Data.class.getSimpleName();

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

	public static void syncEvents() {
		ParseQuery<Event> query = ParseQuery.getQuery(Event.class.getSimpleName());
		query.orderByAscending(Event.ORDER_INDEX);
		List<Event> events;
		try {
			events = query.find();
			Log.d(TAG, "Retrieved " + events.size() + " events");
			ParseObject.unpinAll(Event.CLASS_NAME);
			Log.d(TAG, "All old events unppinned from local database");
			ParseObject.pinAll(Event.CLASS_NAME, events);
			Log.d(TAG, "All new events pinned to local database");
		} catch (ParseException e) {
			Log.e(TAG, "Error synchronizing events: " + e.getMessage());
		}
	}

	public static void syncChecks() {
		ParseQuery<EventCheck> query = ParseQuery.getQuery(EventCheck.class.getSimpleName());
		query.orderByAscending(EventCheck.DATE);
		List<EventCheck> checks;
		try {
			checks = query.find();
			ParseObject.unpinAll(EventCheck.CLASS_NAME);
			for (EventCheck c : checks) {
				Event e = c.getEvent();
				if (e == null || c.getDate() < e.getStartDate() || c.getDate() > e.getEndDate())
					c.deleteEventually();
				else
					c.pin();
			}
		} catch (ParseException e) {
			Log.e(TAG, "Error synchronizing events: " + e.getMessage());
		}
	}


	public static void updateOrderIndex() {
		ParseQuery<Event> query = ParseQuery.getQuery(Event.class.getSimpleName());
		query.orderByAscending(Event.ORDER_INDEX);
		query.fromLocalDatastore();
		try {
			List<Event> events = query.find();
			for (int i = 0; i < events.size(); ++i) {
				Event event = events.get(i);
				event.setOrderIndex(i + 1);
				event.saveEventually();
			}
		} catch (ParseException e) {
			Log.e(TAG, "Error reading local database: " + e.getMessage());
		}
	}
}
