package com.xizz.scoreoflife;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.xizz.scoreoflife.object.Event;
import com.xizz.scoreoflife.object.EventCheck;

public class Application extends android.app.Application {
	@Override
	public void onCreate() {
		super.onCreate();

		ParseObject.registerSubclass(Event.class);
		ParseObject.registerSubclass(EventCheck.class);

		Parse.enableLocalDatastore(this);
		Parse.initialize(this);
		ParseACL.setDefaultACL(new ParseACL(), true);
		Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
	}
}
