package org.briarproject.briar.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;

import junit.framework.AssertionFailedError;

import org.briarproject.briar.R;
import org.briarproject.briar.android.TestComponent;
import org.briarproject.briar.android.navdrawer.NavDrawerActivity;
import org.briarproject.briar.android.test.ScreenshotTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest extends ScreenshotTest {

	@Rule
	public ActivityTestRule<SettingsActivity> activityRule =
			new ActivityTestRule<>(SettingsActivity.class);

	@Override
	protected void inject(TestComponent component) {
		component.inject(this);
	}

	@Override
	protected Activity getActivity() {
		return activityRule.getActivity();
	}

	@Test
	public void changeTheme() {
		onView(withText(R.string.settings_button))
				.check(matches(isDisplayed()));
		onView(withText(R.string.pref_theme_title))
				.check(matches(isDisplayed()))
				.perform(click());
		onView(withText(R.string.pref_theme_light))
				.check(matches(isDisplayed()))
				.perform(click());

		screenshot("manual_dark_theme_settings");

		onView(withText(R.string.pref_theme_title))
				.check(matches(isDisplayed()))
				.perform(click());
		onView(withText(R.string.pref_theme_dark))
				.check(matches(isDisplayed()))
				.perform(click());

		Intent i =
				new Intent(activityRule.getActivity(), NavDrawerActivity.class);
		activityRule.getActivity().startActivity(i);

		try {
			onView(withId(R.id.expiryWarningClose))
					.check(matches(isDisplayed()));
			onView(withId(R.id.expiryWarningClose))
					.perform(click());
		} catch (AssertionFailedError e){
			// TODO remove try block when starting with fresh account
			// ignore since we already removed the expiry warning
		}

		onView(withId(R.id.drawer_layout))
				.check(matches(isClosed(Gravity.LEFT)))
				.perform(DrawerActions.open());

		screenshot("manual_dark_theme_nav_drawer");
	}

}
