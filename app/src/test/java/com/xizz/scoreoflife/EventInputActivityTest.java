package com.xizz.scoreoflife;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 16)
public class EventInputActivityTest {
	private EventInputActivity activity;

	@Before
	public void setUp() throws Exception {
		activity = Robolectric.setupActivity(EventInputActivity.class);
	}

	@Test
	public void testActivityExist() throws Exception {
		assertNotNull(activity);
	}
}