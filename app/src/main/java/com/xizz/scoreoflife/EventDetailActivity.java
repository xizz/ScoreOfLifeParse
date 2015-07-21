package com.xizz.scoreoflife;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.util.Util;

import java.sql.Date;

public class EventDetailActivity extends Activity {

	private TextView mNameView;
	private TextView mScoreView;
	private TextView mStartDateView;
	private TextView mEndDateView;
	private Event mEvent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		Intent intent = getIntent();

		mNameView = (TextView) findViewById(R.id.textViewName);
		mScoreView = (TextView) findViewById(R.id.textViewScore);
		mStartDateView = (TextView) findViewById(R.id.textViewStartDate);
		mEndDateView = (TextView) findViewById(R.id.textViewEndDate);

		ParseQuery<Event> query = ParseQuery.getQuery(Event.class.getSimpleName());
		query.fromLocalDatastore();
		try {
			mEvent = query.get(intent.getStringExtra(Event.ID));
			if (mEvent == null) {
				query.whereEqualTo(Event.NAME, intent.getStringExtra(Event.NAME));
				query.whereEqualTo(Event.SCORE, intent.getIntExtra(Event.SCORE, 0));
				query.whereEqualTo(Event.START_DATE, intent.getLongExtra(Event.START_DATE, 0));
				query.whereEqualTo(Event.END_DATE, intent.getLongExtra(Event.END_DATE,
						Long.MAX_VALUE));
				query.whereEqualTo(Event.ORDER_INDEX, intent.getIntExtra(Event.ORDER_INDEX, 0));
				mEvent = query.getFirst();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		updateDisplay();

		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.event_detail, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			case R.id.edit:
				editEvent();
				break;
			case R.id.delete:
				deleteEvent();
				break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode != Util.REQUEST_EDIT || resultCode != RESULT_OK || data == null)
			return;

		mEvent.setName(data.getStringExtra(Event.NAME));
		mEvent.setScore(data.getIntExtra(Event.SCORE, 0));
		mEvent.setStartDate(data.getLongExtra(Event.START_DATE, 0));
		mEvent.setEndDate(data.getLongExtra(Event.END_DATE, Long.MAX_VALUE));
		mEvent.saveEventually();

		updateDisplay();
	}

	private void editEvent() {
		Intent inputIntent = new Intent(this, EventInputActivity.class);
		inputIntent.putExtra(Event.NAME, mEvent.getName());
		inputIntent.putExtra(Event.SCORE, mEvent.getScore());
		inputIntent.putExtra(Event.START_DATE, mEvent.getStartDate());
		inputIntent.putExtra(Event.END_DATE, mEvent.getEndDate());
		startActivityForResult(inputIntent, Util.REQUEST_EDIT);
	}

	private void deleteEvent() {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setMessage(mEvent + Util.NEWLINE + Util.NEWLINE + "Delete this event?");
		alertBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mEvent.deleteEventually();
				finish();
			}
		});
		alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alertBuilder.show();
	}

	private void updateDisplay() {
		mNameView.setText(mEvent.getName());
		mScoreView.setText("Score: " + mEvent.getScore());
		mStartDateView.setText("Start Date: " + new Date(mEvent.getStartDate()));
		String endDate = mEvent.getEndDate() == Long.MAX_VALUE ?
				"Not Set" : new Date(mEvent.getEndDate()).toString();
		mEndDateView.setText("End Date: " + endDate);
	}
}
