package com.xizz.scoreoflife;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseUser;
import com.parse.ui.ParseLoginActivity;
import com.xizz.scoreoflife.adapter.ChecksPagerAdapter;
import com.xizz.scoreoflife.db.DataSource;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.util.Util;

public class MainActivity extends Activity {

	private DataSource mSource;
	private long mDisplayDate = -1;
	private ViewPager mPager;
	private ChecksPagerAdapter mAdapter;

	private static int dateToIndex(long date, long startDate) {
		return (int) ((date - startDate) / Util.ONEDAY);
	}

	private static long indexToDate(int index, long startDate) {
		return startDate + index * Util.ONEDAY;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSource = DataSource.getDataSource(this);
		mSource.open();

		mPager = (ViewPager) findViewById(R.id.pager);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (ParseUser.getCurrentUser() == null)
			startActivity(new Intent(this, ParseLoginActivity.class));

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

	@Override
	protected void onPause() {
		mDisplayDate = indexToDate(mPager.getCurrentItem(),
				mAdapter.getFirstDay());
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mSource.close();
		super.onDestroy();
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
				break;
		}
	}

}
