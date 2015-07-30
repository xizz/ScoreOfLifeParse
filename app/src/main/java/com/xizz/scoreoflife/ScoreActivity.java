package com.xizz.scoreoflife;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.parse.ParseException;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.object.EventCheck;
import com.xizz.scoreoflife.util.Data;
import com.xizz.scoreoflife.util.Util;

import java.text.MessageFormat;
import java.util.List;

public class ScoreActivity extends Activity {

	private final static long TODAY = Util.getToday();

	private static int getTotalScore(List<Event> events, int days) {
		int total = 0;
		for (int i = 1; i <= days; ++i) {
			for (Event e : events) {
				long day = TODAY - Util.ONEDAY * i;
				if (e.getStartDate() <= day && e.getEndDate() >= day)
					total += e.getScore();
			}
		}
		return total;
	}

	private static int getScore(List<EventCheck> checks, int days) {
		int score = 0;
		for (EventCheck c : checks) {
			// The event start date might be modified, so we should make sure
			// the check date is after the event start date
			if (c.getDone()
					&& c.getDate() >= c.getEvent().getStartDate()
					&& c.getDate() <= c.getEvent().getEndDate()
					&& c.getDate() >= (TODAY - Util.ONEDAY * days)
					&& c.getDate() < TODAY) {
				score += c.getEvent().getScore();
			}
		}
		return score;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score);

		List<EventCheck> checks = null;
		List<Event> events = null;
		try {
			checks = Data.getLocalEventChecks(TODAY - Util.ONEDAY * 30, System.currentTimeMillis
					());
			events = Data.getAllLocalEvents();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// calculate the scores of week and month
		final int weekTotal = getTotalScore(events, 7);
		final int monthTotal = getTotalScore(events, 30);
		final int weekScore = getScore(checks, 7);
		final int monthScore = getScore(checks, 30);

		TextView line1 = (TextView) findViewById(R.id.scoreText1);
		TextView line2 = (TextView) findViewById(R.id.scoreText2);
		TextView line3 = (TextView) findViewById(R.id.scoreText3);
		TextView line4 = (TextView) findViewById(R.id.scoreText4);

		line1.setText("Past 7 days: " + weekScore + "/" + weekTotal);
		line2.setText("Completion: " + MessageFormat.format("{0,number,#.##%}",
				weekScore * 1.0 / weekTotal));
		line3.setText("Past 30 days: " + monthScore + "/" + monthTotal);
		line4.setText("Completion: " + MessageFormat.format("{0,number,#.##%}",
				monthScore * 1.0 / monthTotal));
	}
}