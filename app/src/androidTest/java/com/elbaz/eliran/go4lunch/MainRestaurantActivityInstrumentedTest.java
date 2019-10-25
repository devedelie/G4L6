package com.elbaz.eliran.go4lunch;

import androidx.test.rule.ActivityTestRule;

import com.elbaz.eliran.go4lunch.controllers.activities.SplashScreen;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.repeatedlyUntil;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.anyOf;

/**
 * Created by Eliran Elbaz on 25-Oct-19.
 */
public class MainRestaurantActivityInstrumentedTest {

    @Rule
    public ActivityTestRule<SplashScreen> mActivityTestRule = new ActivityTestRule<>(SplashScreen.class);

    @Test
    public void MainRestaurantActivity_whenUserIsAuthentified_verifyViewElementsAndFragments() throws Exception {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Verify all MainRestaurantActivity view elements & Fragments
        onView(withId(R.id.activity_main_bottom_navigation)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.activity_main_restaurant_viewpager)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.toolbar)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.main_restaurant_activity_drawerLayout)).check(matches(isCompletelyDisplayed()));

        // Switch fragments on BottomNavigation
        onView(anyOf(withId(R.id.action_listView))).perform(click()); // Switch to ListView Fragment
        onView(withId(R.id.fragment_list_view)).check(matches(isCompletelyDisplayed())); // Verify ListView view elements
        onView(withId(R.id.listView_recyclerView)).check(matches(isCompletelyDisplayed())); // Verify ListView recyclerView

        onView(anyOf(withId(R.id.action_workmates))).perform(click());
        onView(withId(R.id.fragment_workmates)).check(matches(isCompletelyDisplayed())); // Verify Workmates view elements
        onView(withId(R.id.workmateView_recyclerView)).check(matches(isCompletelyDisplayed())); // Verify Workmates recyclerView

        onView(anyOf(withId(R.id.action_mapView))).perform(click());

        // Scroll fragments with ViewPager
        int maxAttempts=3;
        onView(withId(R.id.activity_main_restaurant_viewpager)).perform(repeatedlyUntil(swipeLeft(), hasDescendant(withId(R.id.map_view_fragment)), maxAttempts));
        onView(withId(R.id.activity_main_restaurant_viewpager)).perform(repeatedlyUntil(swipeLeft(), hasDescendant(withId(R.id.fragment_list_view)), maxAttempts));
        onView(withId(R.id.activity_main_restaurant_viewpager)).perform(repeatedlyUntil(swipeLeft(), hasDescendant(withId(R.id.fragment_workmates)), maxAttempts));
    }
}