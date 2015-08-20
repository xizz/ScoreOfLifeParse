package com.xizz.scoreoflife;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginActivity;
import com.xizz.scoreoflife.adapter.ChecksPagerAdapter;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.util.Data;
import com.xizz.scoreoflife.util.Util;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private long mDisplayDate = -1;
	private ViewPager mPager;
	private ChecksPagerAdapter mAdapter;


	private static int dateToIndex(long date, long startDate) {
		return (int) ((date - startDate) / Util.DAY_MILLI_SECS);
	}

	private static long indexToDate(int index, long startDate) {
		return startDate + index * Util.DAY_MILLI_SECS;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPager = (ViewPager) findViewById(R.id.pager);

		syncFromCloud();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (ParseUser.getCurrentUser() == null)
			startActivityForResult(new Intent(this, ParseLoginActivity.class), Util.REQUEST_LOGIN);

		loadEventCheckList();
	}

	@Override
	protected void onPause() {
		mDisplayDate = indexToDate(mPager.getCurrentItem(), mAdapter.getFirstDay());
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_event:
				startActivityForResult(new Intent(this, EventInputActivity.class),
						Util.REQUEST_ADD);
				break;
			case R.id.my_score:
				startActivity(new Intent(this, ScoreActivity.class));
				break;
			case R.id.manage_events:
				startActivity(new Intent(this, EventsActivity.class));
				break;
			case R.id.sync:
				syncFromCloud();
				break;
			case R.id.sign_out:
				ParseUser.logOut();
				startActivity(new Intent(this, ParseLoginActivity.class));
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
			case Util.REQUEST_ADD:
				Event event = new Event();
				event.setName(data.getStringExtra(Event.NAME));
				event.setScore(data.getIntExtra(Event.SCORE, 0));
				event.setStartDate(data.getLongExtra(Event.START_DATE, 0));
				event.setEndDate(data.getLongExtra(Event.END_DATE, Long.MAX_VALUE));
				event.setOrderIndex(0);
				try {
					event.pin(Event.CLASS_NAME);
					event.saveEventually();
				} catch (ParseException e) {
					Log.e(TAG, "Error pinning event: " + e.getMessage());
				}
				Data.updateOrderIndex();
				break;
			case Util.REQUEST_LOGIN:
				Log.d(TAG, "Logged in, sync now");
				syncFromCloud();
				break;
		}
	}

	private void syncFromCloud() {
		// pull latest events and checks from cloud
		Thread thread = new Thread(new Sync());
		thread.start();
		Log.d(TAG, "started thread: " + thread.getName());
	}

	private void loadEventCheckList() {
		if (mPager == null)
			return;
		mAdapter = new ChecksPagerAdapter(this);
		mPager.setAdapter(mAdapter);
		// mPosition could be saved from previous state.
		// Set it to the current day be default.
		if (mDisplayDate == -1) {
			mDisplayDate = Util.getToday();
		}
		if (mDisplayDate < mAdapter.getFirstDay()) {
			mDisplayDate = mAdapter.getFirstDay();
		}
		mPager.setCurrentItem(dateToIndex(mDisplayDate, mAdapter.getFirstDay()));
	}

	private class Sync implements Runnable {
		private final String TAG = Sync.class.getSimpleName();

		@Override
		public void run() {
			try {
				Data.syncEvents();
				Data.syncChecks();

			} catch (ParseException e) {
				Log.e(TAG, "Error synchronizing events: " + e.getMessage());
			}

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loadEventCheckList();
					Log.d(TAG, "loaded event list after synchronization");
				}
			});
		}
	}
}
