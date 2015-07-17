package com.xizz.scoreoflife;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.xizz.scoreoflife.db.DataSource;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.util.Util;

import java.sql.Date;

public class EventDetailActivity extends Activity {

	private DataSource mSource;
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

		mSource = DataSource.getDataSource(this);

		mNameView = (TextView) findViewById(R.id.textViewName);
		mScoreView = (TextView) findViewById(R.id.textViewScore);
		mStartDateView = (TextView) findViewById(R.id.textViewStartDate);
		mEndDateView = (TextView) findViewById(R.id.textViewEndDate);

		mEvent = new Event();
		mEvent.id = intent.getLongExtra(Util.ID, 0);
		mEvent.name = intent.getStringExtra(Util.NAME);
		mEvent.score = intent.getIntExtra(Util.SCORE, 0);
		mEvent.startDate = intent.getLongExtra(Util.START_DATE, 0);
		mEvent.endDate = intent.getLongExtra(Util.END_DATE, 0);
		mEvent.orderIndex = intent.getIntExtra(Util.ORDER_INDEX, 0);

		mNameView.setText(mEvent.name);
		mScoreView.setText("Score: " + mEvent.score);
		mStartDateView.setText("Start Date: " + new Date(mEvent.startDate));
		String endDate = mEvent.endDate == Long.MAX_VALUE ? "Not Set"
				: new Date(mEvent.endDate).toString();
		mEndDateView.setText("End Date: " + endDate);

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

		if (requestCode != Util.REQUEST_EDIT || resultCode != RESULT_OK
				|| data == null) {
			return;
		}

		mEvent.name = data.getStringExtra(Util.NAME);
		mEvent.score = data.getIntExtra(Util.SCORE, 0);
		mEvent.startDate = data.getLongExtra(Util.START_DATE, 0);
		mEvent.endDate = data.getLongExtra(Util.END_DATE, Long.MAX_VALUE);

		mSource.updateEvent(mEvent);

		mNameView.setText(mEvent.name);
		mScoreView.setText("Score: " + mEvent.score);
		mStartDateView.setText("Start Date: " + new Date(mEvent.startDate));
		String endDate = mEvent.endDate == Long.MAX_VALUE ? "Not Set"
				: new Date(mEvent.endDate).toString();
		mEndDateView.setText("End Date: " + endDate);
	}

	private void editEvent() {
		Intent inputIntent = new Intent(this, EventInputActivity.class);
		inputIntent.putExtra(Util.NAME, mEvent.name);
		inputIntent.putExtra(Util.SCORE, mEvent.score);
		inputIntent.putExtra(Util.START_DATE, mEvent.startDate);
		inputIntent.putExtra(Util.END_DATE, mEvent.endDate);
		startActivityForResult(inputIntent, Util.REQUEST_EDIT);
	}

	private void deleteEvent() {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setMessage(mEvent + Util.NEWLINE + Util.NEWLINE
				+ "Delete this event?");
		alertBuilder.setPositiveButton("Delete",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSource.deleteEvent(mEvent);
						finish();
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

}
