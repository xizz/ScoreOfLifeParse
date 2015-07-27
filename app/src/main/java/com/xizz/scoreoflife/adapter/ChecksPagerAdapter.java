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
import com.parse.ParseQuery;
import com.xizz.scoreoflife.R;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.object.EventCheck;
import com.xizz.scoreoflife.util.Data;
import com.xizz.scoreoflife.util.Util;

import java.util.List;

public class ChecksPagerAdapter extends PagerAdapter {
	private static final String TAG = ChecksPagerAdapter.class.getSimpleName();

	private static final long TODAY = Util.getToday();
	private static final long DAY_MILLI_SECS = 86400000;

	private long mFirstDay;
	private Context mContext;
	private LayoutInflater mInflater;
	private List<Event> mEvents;

	public ChecksPagerAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ParseQuery<Event> query = ParseQuery.getQuery(Event.CLASS_NAME);
		query.fromLocalDatastore();

		// TODO: there might be a limit for query all
		try {
			mEvents = query.find();
		} catch (ParseException e) {
			Log.e(TAG, "Error: " + e.getMessage());
		}
		mFirstDay = getEarliestDate(mEvents);
	}

	private static long getEarliestDate(List<Event> events) {
		if (events == null || events.isEmpty())
			return TODAY;
		Event ealiestEvent = events.get(0);
		for (Event e : events) {
			if (e.getStartDate() < ealiestEvent.getStartDate()) {
				ealiestEvent = e;
			}
		}

		return ealiestEvent.getStartDate();
	}

	private static boolean eventExist(Event event, List<EventCheck> checks) {
		for (EventCheck c : checks) {
			if (event.getObjectId().equals(c.getEvent().getObjectId()))
				return true;
		}
		return false;
	}

	@Override
	public int getCount() {
		return (int) ((TODAY - mFirstDay) / DAY_MILLI_SECS) + 1;
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

		long date = mFirstDay + DAY_MILLI_SECS * position;

		try {
			createChecksIfNotExist(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<EventCheck> checks = null;
		try {
			checks = Data.getEventChecks(date, date + DAY_MILLI_SECS - 1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//TODO: Evaluate the following. Are they still needed?
		Util.removeLegacyChecks(mEvents, checks);
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
		long date = mFirstDay + DAY_MILLI_SECS * position;
		return new java.sql.Date(date).toString();
	}

	public long getFirstDay() {
		return mFirstDay;
	}

	private void createChecksIfNotExist(long date) throws ParseException {
		List<EventCheck> checks = Data.getEventChecks(date, date + DAY_MILLI_SECS - 1);
		for (Event e : mEvents) {
			if (date >= e.getStartDate() && date <= e.getEndDate() && !eventExist(e, checks)) {
				EventCheck check = new EventCheck(e, date);
				check.pin();
				check.saveEventually();
			}
		}
	}
}
