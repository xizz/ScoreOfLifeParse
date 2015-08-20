package com.xizz.scoreoflife.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseException;
import com.xizz.scoreoflife.R;
import com.xizz.scoreoflife.object.EventCheck;
import com.xizz.scoreoflife.util.Data;
import com.xizz.scoreoflife.util.Util;

import java.util.Collections;
import java.util.List;

public class ChecksPagerAdapter extends PagerAdapter {
	private static final String TAG = ChecksPagerAdapter.class.getSimpleName();

	private static final long TODAY = Util.getToday();

	private long mFirstDay;
	private Context mContext;
	private LayoutInflater mInflater;

	public ChecksPagerAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		try {
			mFirstDay = Data.getEarliestDate();
		} catch (ParseException e) {
			Log.d(TAG, "Error finding first day: " + e.getMessage());
			mFirstDay = TODAY;
		}
	}

	@Override
	public int getCount() {
		return (int) ((TODAY - mFirstDay) / Util.DAY_MILLI_SECS) + 1;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = mInflater.inflate(R.layout.checklist, container, false);

		ListView checksList = (ListView) view.findViewById(R.id.checklist);
		TextView emptyText = (TextView) view.findViewById(android.R.id.empty);

		checksList.setEmptyView(emptyText);

		long date = mFirstDay + Util.DAY_MILLI_SECS * position;

		List<EventCheck> checks = null;

		try {
			Data.createChecksIfNotExist(date);
			checks = Data.getLocalEventChecks(date, date + Util.DAY_MILLI_SECS - 1);
			Collections.sort(checks);
			Data.removeDuplicateChecks(checks);
			Data.removeLegacyChecks(checks);
		} catch (ParseException e) {
			Log.e(TAG, "Error: " + e.getMessage());
		}

		checksList.setAdapter(new ChecksAdapter(mContext, checks));

		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		long date = mFirstDay + Util.DAY_MILLI_SECS * position;
		return new java.sql.Date(date).toString();
	}

	public long getFirstDay() {
		return mFirstDay;
	}
}
