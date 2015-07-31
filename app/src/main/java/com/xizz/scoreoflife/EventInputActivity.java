package com.xizz.scoreoflife;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;

import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.util.Util;

import java.sql.Date;
import java.text.ParseException;

public class EventInputActivity extends Activity {
	private static final String TAG = EventInputActivity.class.getSimpleName();

	private DatePickerDialog mStartDatePicker;
	private DatePickerDialog mEndDatePicker;
	private EditText mNameView;
	private EditText mScoreView;
	private EditText mStartDateView;
	private EditText mEndDateView;
	private CheckBox mCheckEndDate;
	private Button mBtnPickEndDate;

	private void initialize() {
		mNameView = (EditText) findViewById(R.id.editTextName);
		mScoreView = (EditText) findViewById(R.id.editTextScore);
		mStartDateView = (EditText) findViewById(R.id.editTextStartDate);
		mEndDateView = (EditText) findViewById(R.id.editTextEndDate);
		mCheckEndDate = (CheckBox) findViewById(R.id.checkBoxEndDate);
		mBtnPickEndDate = (Button) findViewById(R.id.buttonEndDate);

		mCheckEndDate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mBtnPickEndDate.setClickable(isChecked);
				mEndDateView.setEnabled(isChecked);
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input);

		initialize();

		Intent intent = getIntent();
		long startDate = intent.getLongExtra(Event.START_DATE, 0);

		// Find out if this is call from new or edit.
		if (startDate != 0) {
			mNameView.setText(intent.getStringExtra(Event.NAME));
			mScoreView.setText(Integer.toString(intent.getIntExtra(Event.SCORE, 0)));
			mStartDateView.setText(new Date(startDate).toString());
			setTitle("Edit Event");
		} else {
			mStartDateView.setText(new Date(System.currentTimeMillis()).toString());
		}

		setStartDatePicker();
		setEndDatePicker();

		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.event_input, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			case R.id.done_input:
				doneInput();
				break;
		}
		return true;
	}

	private void doneInput() {
		String name = mNameView.getText().toString();
		String score = mScoreView.getText().toString();
		String startDate = mStartDateView.getText().toString();
		String endDate = mEndDateView.getText().toString();
		boolean error = false;
		if (name.length() == 0) {
			mNameView.setError("Missing event name.");
			error = true;
		}
		if (score.length() == 0) {
			mScoreView.setError("Missing event score.");
			error = true;
		}
		if (mCheckEndDate.isChecked() && startDate.compareTo(endDate) > 0) {
			mEndDateView.setError("The end date is earlier then start date.");
			error = true;
		}
		if (error)
			return;
		Intent output = new Intent();
		output.putExtra(Event.NAME, mNameView.getText().toString());
		output.putExtra(Event.SCORE, Integer.parseInt(score));
		try {
			output.putExtra(Event.START_DATE, Util.DATE_FORMAT.parse(startDate).getTime());
			if (mCheckEndDate.isChecked())
				output.putExtra(Event.END_DATE, Util.DATE_FORMAT.parse(endDate).getTime());
		} catch (ParseException e) {
			Log.e(TAG, "Error: " + e.getMessage());
		}
		setResult(RESULT_OK, output);
		finish();
	}

	public void pickStartDate(View view) {
		mStartDatePicker.show();
	}

	public void pickEndDate(View view) {
		mEndDatePicker.show();
	}

	private void setStartDatePicker() {
		long startDate = getIntent().getLongExtra(Event.START_DATE, 0);
		if (startDate != 0) {
			mStartDateView.setText(new Date(startDate).toString());
		} else {
			mStartDateView.setText(new Date(System.currentTimeMillis())
					.toString());
		}
		String date = mStartDateView.getText().toString();
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(5, 7)) - 1;
		int day = Integer.parseInt(date.substring(8, 10));

		OnDateSetListener listener = new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mStartDateView.setText("" + year + "-"
						+ String.format("%02d", (monthOfYear + 1)) + "-"
						+ String.format("%02d", dayOfMonth));
			}
		};
		mStartDatePicker = new DatePickerDialog(this, listener, year, month, day);
	}

	private void setEndDatePicker() {
		long endDate = getIntent().getLongExtra(Event.END_DATE, Long.MAX_VALUE);
		boolean hasEndDate = endDate != Long.MAX_VALUE;
		mCheckEndDate.setChecked(hasEndDate);
		mBtnPickEndDate.setClickable(hasEndDate);
		mEndDateView.setEnabled(hasEndDate);

		if (hasEndDate)
			mEndDateView.setText(new Date(endDate).toString());
		else
			mEndDateView.setText(new Date(System.currentTimeMillis()).toString());

		String date = mEndDateView.getText().toString();
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(5, 7)) - 1;
		int day = Integer.parseInt(date.substring(8, 10));

		OnDateSetListener listener = new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mEndDateView.setText("" + year + "-"
						+ String.format("%02d", (monthOfYear + 1)) + "-"
						+ String.format("%02d", dayOfMonth));
			}
		};
		mEndDatePicker = new DatePickerDialog(this, listener, year, month, day);
	}
}
