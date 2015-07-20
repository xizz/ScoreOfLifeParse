package com.xizz.scoreoflife;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.parse.ParseUser;
import com.parse.ui.ParseLoginActivity;
import com.xizz.scoreoflife.adapter.EventsAdapter;
import com.xizz.scoreoflife.db.DataSource;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.util.Util;

import java.util.List;

public class EventsActivity extends Activity implements
		OnItemLongClickListener, OnItemClickListener {

	private final static int CURRENT_EVENTS = 11;
	private final static int PAST_EVENTS = 22;
	private final static int FUTURE_EVENTS = 33;

	private DataSource mSource;
	private ListView mEventsView;
	private EventsAdapter mAdapter;
	private Event mEventClicked;
	private int mCurrentList = CURRENT_EVENTS;
	private long mToday = Util.getToday();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		mSource = DataSource.getDataSource(this);

		mEventsView = (ListView) findViewById(R.id.eventsList);
		mEventsView.setOnItemLongClickListener(this);
		mEventsView.setOnItemClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		loadEventList();
		if (ParseUser.getCurrentUser() == null)
			startActivity(new Intent(this, ParseLoginActivity.class));
	}

	private void loadEventList() {
		List<Event> events = null;

		switch (mCurrentList) {
			case CURRENT_EVENTS:
				events = mSource.getEvents(mToday);
				break;
			case PAST_EVENTS:
				events = mSource.getPastEvents(mToday);
				break;
			case FUTURE_EVENTS:
				events = mSource.getFutureEvents(mToday);
				break;
		}

		mAdapter = new EventsAdapter(this, events);
		mEventsView.setAdapter(mAdapter);
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
			case R.id.sign_out:
				ParseUser.logOut();
				startActivity(new Intent(this, ParseLoginActivity.class));
				break;
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
	                        long id) {
		final Event event = (Event) mAdapter.getItem(position);
		Intent intent = new Intent(this, EventDetailActivity.class);
		intent.putExtra(Util.ID, event.id);
		intent.putExtra(Util.NAME, event.name);
		intent.putExtra(Util.SCORE, event.score);
		intent.putExtra(Util.START_DATE, event.startDate);
		intent.putExtra(Util.END_DATE, event.endDate);
		intent.putExtra(Util.ORDER_INDEX, event.orderIndex);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
	                               int position, long id) {
		mEventClicked = (Event) mAdapter.getItem(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// The first item cannot move up and the last item cannot move down,
		// so the menu items needs to be handled differently.
		if (mAdapter.getCount() == 1) {
			builder.setItems(new String[]{"Edit", "Delete"},
					getOnlyMenu(position));
		} else if (position == 0) {
			builder.setItems(new String[]{"Edit", "Delete", "Move Down"},
					getFirstMenu(position));
		} else if (position == mAdapter.getCount() - 1) {
			builder.setItems(new String[]{"Move Up", "Edit", "Delete"},
					getLastMenu(position));
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

		if (resultCode != RESULT_OK || data == null) {
			return;
		}

		switch (requestCode) {
			case Util.REQUEST_ADD:
				Event event = new Event();
				event.name = data.getStringExtra(Util.NAME);
				event.score = data.getIntExtra(Util.SCORE, 0);
				event.startDate = data.getLongExtra(Util.START_DATE, 0);
				event.endDate = data.getLongExtra(Util.END_DATE, Long.MAX_VALUE);
				mSource.insertEvent(event);
				loadEventList();
				break;
			case Util.REQUEST_EDIT:
				mEventClicked.name = data.getStringExtra(Util.NAME);
				mEventClicked.score = data.getIntExtra(Util.SCORE, 0);
				mEventClicked.startDate = data.getLongExtra(Util.START_DATE, 0);
				mEventClicked.endDate = data.getLongExtra(Util.END_DATE,
						Long.MAX_VALUE);
				mSource.updateEvent(mEventClicked);
				loadEventList();
				break;
		}
	}

	private void editEvent(Event event) {
		Intent inputIntent = new Intent(EventsActivity.this,
				EventInputActivity.class);
		inputIntent.putExtra(Util.NAME, event.name);
		inputIntent.putExtra(Util.SCORE, event.score);
		inputIntent.putExtra(Util.START_DATE, event.startDate);
		inputIntent.putExtra(Util.END_DATE, event.endDate);
		startActivityForResult(inputIntent, Util.REQUEST_EDIT);
	}

	private void askForDelete(final Event event) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setMessage(event + Util.NEWLINE + Util.NEWLINE
				+ "Delete this item?");
		alertBuilder.setPositiveButton("Delete",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSource.deleteEvent(event);
						mAdapter.remove(event);
						mAdapter.notifyDataSetChanged();
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
						swapIndex((Event) mAdapter.getItem(position - 1),
								mEventClicked);
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
						swapIndex((Event) mAdapter.getItem(position - 1),
								mEventClicked);
						break;
					case 1:
						editEvent(mEventClicked);
						break;
					case 2:
						askForDelete(mEventClicked);
						break;
					case 3:
						swapIndex((Event) mAdapter.getItem(position + 1),
								mEventClicked);
						break;
				}
			}
		};
	}

	private void swapIndex(Event e1, Event e2) {
		int tempIndex = e1.orderIndex;
		e1.orderIndex = e2.orderIndex;
		e2.orderIndex = tempIndex;
		mSource.updateEvent(e1);
		mSource.updateEvent(e2);
		loadEventList();
	}
}
