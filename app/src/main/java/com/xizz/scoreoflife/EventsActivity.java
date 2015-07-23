package com.xizz.scoreoflife;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginActivity;
import com.xizz.scoreoflife.adapter.EventsAdapter;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.util.Util;

import java.util.List;
import java.util.Set;

public class EventsActivity extends Activity implements
		OnItemLongClickListener, OnItemClickListener {

	private static final String TAG = EventsActivity.class.getSimpleName();

	private final static int CURRENT_EVENTS = 11;
	private final static int PAST_EVENTS = 22;
	private final static int FUTURE_EVENTS = 33;

	private ListView mEventsView;
	private EventsAdapter mAdapter;
	private Event mEventClicked;
	private int mCurrentList = CURRENT_EVENTS;
	private long mToday = Util.getToday();

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		mEventsView = (ListView) findViewById(R.id.eventsList);
		mEventsView.setOnItemLongClickListener(this);
		mEventsView.setOnItemClickListener(this);

		syncFromCloud();
	}

	@Override
	protected void onResume() {
		super.onResume();

		loadEventList();
		if (ParseUser.getCurrentUser() == null)
			startActivity(new Intent(this, ParseLoginActivity.class));
	}

	private void loadEventList() {

		ParseQuery<Event> query = ParseQuery.getQuery(Event.class.getSimpleName());
		query.fromLocalDatastore();
		query.orderByAscending(Event.ORDER_INDEX);
		switch (mCurrentList) {
			case CURRENT_EVENTS:
				query.whereGreaterThanOrEqualTo(Event.END_DATE, mToday);
				query.whereLessThanOrEqualTo(Event.START_DATE, mToday);
				break;
			case PAST_EVENTS:
				query.whereLessThan(Event.END_DATE, mToday);
				break;
			case FUTURE_EVENTS:
				query.whereGreaterThan(Event.START_DATE, mToday);
				break;
		}

		try {
			List<Event> events = query.find();
			Log.d(TAG, "Retrieved " + events.size() + " events");
			for (Event event : events)
				Log.d(TAG, "Event ID: " + event.getObjectId());
			mAdapter = new EventsAdapter(EventsActivity.this, events);
			mEventsView.setAdapter(mAdapter);

		} catch (ParseException e) {
			Log.e(TAG, "Error reading local database: " + e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.events, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			case R.id.add_event:
				startActivityForResult(new Intent(this, EventInputActivity.class),
						Util.REQUEST_ADD);
				break;
			case R.id.current_events:
				if (mCurrentList != CURRENT_EVENTS) {
					mCurrentList = CURRENT_EVENTS;
					loadEventList();
				}
				break;
			case R.id.past_events:
				if (mCurrentList != PAST_EVENTS) {
					mCurrentList = PAST_EVENTS;
					loadEventList();
				}
				break;
			case R.id.future_events:
				if (mCurrentList != FUTURE_EVENTS) {
					mCurrentList = FUTURE_EVENTS;
					loadEventList();
				}
				break;
			case R.id.sync:
				syncFromCloud();
				break;
			case R.id.sign_out:
				ParseUser.logOut();
				startActivity(new Intent(this, ParseLoginActivity.class));
				break;
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Event event = (Event) mAdapter.getItem(position);
		Intent intent = new Intent(this, EventDetailActivity.class);
		intent.putExtra(Event.ID, event.getObjectId());
		intent.putExtra(Event.NAME, event.getName());
		intent.putExtra(Event.SCORE, event.getScore());
		intent.putExtra(Event.START_DATE, event.getStartDate());
		intent.putExtra(Event.END_DATE, event.getEndDate());
		intent.putExtra(Event.ORDER_INDEX, event.getOrderIndex());
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
	                               int position, long id) {
		mEventClicked = (Event) mAdapter.getItem(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// The first item cannot move up and the last item cannot move down,
		// so the menu items needs to be handled differently.
		// TODO: should refactor the fallowing
		if (mAdapter.getCount() == 1) {
			builder.setItems(new String[]{"Edit", "Delete"}, getOnlyMenu(position));
		} else if (position == 0) {
			builder.setItems(new String[]{"Edit", "Delete", "Move Down"}, getFirstMenu(position));
		} else if (position == mAdapter.getCount() - 1) {
			builder.setItems(new String[]{"Move Up", "Edit", "Delete"}, getLastMenu(position));
		} else {
			builder.setItems(new String[]{"Move Up", "Edit", "Delete",
					"Move Down"}, getMiddleMenu(position));
		}
		builder.show();
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK || data == null)
			return;

		switch (requestCode) {
			case Util.REQUEST_ADD:
				final Event event = new Event();
				event.setName(data.getStringExtra(Event.NAME));
				event.setScore(data.getIntExtra(Event.SCORE, 0));
				event.setStartDate(data.getLongExtra(Event.START_DATE, 0));
				event.setEndDate(data.getLongExtra(Event.END_DATE, Long.MAX_VALUE));
				event.setOrderIndex(0);
				Log.d(TAG, "Event ID: " + event.getObjectId());
				Log.d(TAG, "event: " + event);

				try {
					event.pin(Event.CLASS_NAME);
					event.saveEventually();
				} catch (ParseException e) {
					Log.e(TAG, "Error pinning event: " + e.getMessage());
				}
				updateOrderIndex();
				loadEventList();
				break;
			case Util.REQUEST_EDIT:
				mEventClicked.setName(data.getStringExtra(Event.NAME));
				mEventClicked.setScore(data.getIntExtra(Event.SCORE, 0));
				mEventClicked.setStartDate(data.getLongExtra(Event.START_DATE, 0));
				mEventClicked.setEndDate(data.getLongExtra(Event.END_DATE, Long.MAX_VALUE));
				mEventClicked.saveEventually();
				mAdapter.notifyDataSetChanged();
				break;
		}
	}

	private void syncFromCloud() {
		// pull latest events from cloud
		Thread thread = new Thread(new Sync());
		thread.start();
		Log.d(TAG, "started thread: " + thread.getName());
	}

	private void editEvent(Event event) {
		Intent intent = new Intent(EventsActivity.this, EventInputActivity.class);
		intent.putExtra(Event.ID, event.getObjectId());
		intent.putExtra(Event.NAME, event.getName());
		intent.putExtra(Event.SCORE, event.getScore());
		intent.putExtra(Event.START_DATE, event.getStartDate());
		intent.putExtra(Event.END_DATE, event.getEndDate());
		intent.putExtra(Event.ORDER_INDEX, event.getOrderIndex());
		startActivityForResult(intent, Util.REQUEST_EDIT);
	}

	private void askForDelete(final Event event) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setMessage(event + Util.NEWLINE + Util.NEWLINE
				+ "Delete this item?");
		alertBuilder.setPositiveButton("Delete",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mAdapter.remove(event);
						mAdapter.notifyDataSetChanged();
						event.deleteEventually();
					}
				});
		alertBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		alertBuilder.show();
	}

	private DialogInterface.OnClickListener getOnlyMenu(final int position) {
		return new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int index) {
				switch (index) {
					case 0:
						editEvent(mEventClicked);
						break;
					case 1:
						askForDelete(mEventClicked);
						break;

				}
			}
		};
	}

	private DialogInterface.OnClickListener getFirstMenu(final int position) {
		return new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int index) {
				switch (index) {
					case 0:
						editEvent(mEventClicked);
						break;
					case 1:
						askForDelete(mEventClicked);
						break;
					case 2:
						swapIndex((Event) mAdapter.getItem(position + 1),
								mEventClicked);
						break;
				}
			}
		};
	}

	private DialogInterface.OnClickListener getLastMenu(final int position) {
		return new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int index) {
				switch (index) {
					case 0:
						swapIndex((Event) mAdapter.getItem(position - 1), mEventClicked);
						break;
					case 1:
						editEvent(mEventClicked);
						break;
					case 2:
						askForDelete(mEventClicked);
						break;
				}
			}
		};
	}

	private DialogInterface.OnClickListener getMiddleMenu(final int position) {
		return new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int index) {
				switch (index) {
					case 0:
						swapIndex((Event) mAdapter.getItem(position - 1), mEventClicked);
						break;
					case 1:
						editEvent(mEventClicked);
						break;
					case 2:
						askForDelete(mEventClicked);
						break;
					case 3:
						swapIndex((Event) mAdapter.getItem(position + 1), mEventClicked);
						break;
				}
			}
		};
	}

	private void swapIndex(Event e1, Event e2) {
		Log.d(TAG, "Swaping: " + e1.getName() + " " + e2.getName());
		int tempIndex = e1.getOrderIndex();
		e1.setOrderIndex(e2.getOrderIndex());
		e2.setOrderIndex(tempIndex);
		e1.saveEventually();
		e2.saveEventually();
		loadEventList();
	}

	private void logRunningThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Thread t : threadSet)
			Log.d(TAG, "Thread " + t.getName());
		Log.d(TAG, "Number of threads: " + threadSet.size());
	}

	private class Sync implements Runnable {
		private final String TAG = Sync.class.getSimpleName();

		@Override
		public void run() {
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
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loadEventList();
					Log.d(TAG, "loaded event list after synchronization");
				}
			});
		}
	}
}
