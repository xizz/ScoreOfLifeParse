package com.xizz.scoreoflife.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

	public final static String TABLE_EVENTS = "events";
	public final static String TABLE_CHECKS = "event_checks";

	public final static String ID = "_id";
	public final static String NAME = "name";
	public final static String SCORE = "score";
	public final static String START_DATE = "start_date";
	public final static String END_DATE = "end_date";
	public final static String ORDER_INDEX = "order_index";

	public final static String IS_DONE = "is_done";
	public final static String EVENT_ID = "event_id";
	public final static String DATE = "date";

	public DBOpenHelper(Context context) {
		super(context, "scoremylife.db", null, 3);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_EVENTS + " ( " + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT, "
				+ SCORE + " INTEGER, " + START_DATE + " INTEGER, " + END_DATE
				+ " INTEGER, " + ORDER_INDEX + " INTEGER)");
		db.execSQL("CREATE TABLE " + TABLE_CHECKS + "(" + DATE + " INTEGER, "
				+ IS_DONE + " INTEGER, " + EVENT_ID + " INTEGER, FOREIGN KEY("
				+ EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + ID + "))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2)
			upgrade1to2(db);
		else if (oldVersion == 2 && newVersion == 3)
			upgrade2to3(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 2 && newVersion == 1)
			downgrade2to1(db);

	}

	private void downgrade2to1(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE events RENAME TO eventsOld");
		db.execSQL("CREATE TABLE " + TABLE_EVENTS + " ( " + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT, "
				+ SCORE + " INTEGER, " + START_DATE + " INTEGER, " + END_DATE
				+ " INTEGER, " + ORDER_INDEX + " INTEGER)");
		db.execSQL("INSERT INTO events (_id, score, start_date)"
				+ " SELECT _id, name, score, start_date FROM eventsOld");
		db.execSQL("DROP TABLE IF EXISTS eventsOld");

	}

	private void upgrade1to2(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + TABLE_EVENTS + " ADD " + ORDER_INDEX
				+ " INTEGER");
	}

	private void upgrade2to3(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE events RENAME TO eventsOld");
		db.execSQL("CREATE TABLE " + TABLE_EVENTS + " ( " + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT, "
				+ SCORE + " INTEGER, " + START_DATE + " INTEGER, " + END_DATE
				+ " INTEGER DEFAULT 9223372036854775807, " + ORDER_INDEX
				+ " INTEGER)");
		db.execSQL("INSERT INTO events ( _id, name, score, start_date, order_index)"
				+ " select _id, name, score, start_date, order_index FROM eventsOld");
		db.execSQL("DROP TABLE IF EXISTS eventsOld");

	}
}
