package com.elbaz.eliran.go4lunch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.Nullable;
import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;

import com.elbaz.eliran.go4lunch.controllers.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.repeatedlyUntil;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by Eliran Elbaz on 25-Oct-19.
 */
public class MainRestaurantActivityInstrumentedTest {
    private static Context appContext= getInstrumentation().getTargetContext();
    // --------------------------------------Annotation Tags Setup----------------------------------
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void before(){
        System.out.println("Before test");
        // if user is not logged, create user for test
        if(!isCurrentUserLogged()) {
            signInBeforeTest();
        }else{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void after(){
        System.out.println("After test");
        // Delete test user at the end
        if(getCurrentUser().getDisplayName().equals("username")){
            deleteTestUser();
        }

    }
    //---------------------------------------------------------------------------------------------

    @Test
    public void MainRestaurantActivity_verifyViewElementsAndFragments_returnMatches() throws Exception {
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

        onView(anyOf(withId(R.id.action_workmates))).perform(click()); // Switch to Workmates Fragment
        onView(withId(R.id.fragment_workmates)).check(matches(isCompletelyDisplayed())); // Verify Workmates view elements
        onView(withId(R.id.workmateView_recyclerView)).check(matches(isCompletelyDisplayed())); // Verify Workmates recyclerView

        onView(anyOf(withId(R.id.action_mapView))).perform(click()); // Switch back to MapView Fragment

        // Scroll fragments with ViewPager
        int maxAttempts=3;
        onView(withId(R.id.activity_main_restaurant_viewpager)).perform(repeatedlyUntil(swipeLeft(), hasDescendant(withId(R.id.map_view_fragment)), maxAttempts));
        onView(withId(R.id.activity_main_restaurant_viewpager)).perform(repeatedlyUntil(swipeLeft(), hasDescendant(withId(R.id.fragment_list_view)), maxAttempts));
        onView(withId(R.id.activity_main_restaurant_viewpager)).perform(repeatedlyUntil(swipeLeft(), hasDescendant(withId(R.id.fragment_workmates)), maxAttempts));
    }

    @Test
    public void bottomSheetFragment_loadRestaurantAndMarkAsGoing_returnMatches() throws Exception {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(anyOf(withId(R.id.action_listView))).perform(click()); // Switch to ListView Fragment

        // Click on first item on RecyclerView (position 0)
        ViewInteraction cardView = onView(
                Matchers.allOf(childAtPosition(
                        Matchers.allOf(withId(R.id.listView_recyclerView),
                                childAtPosition(
                                        withId(R.id.fragment_list_view),
                                        0)),
                        0),
                        isDisplayed()));
        cardView.perform(click());

        // Verify BottomSheet view display
        ViewInteraction floatingActionButton = onView(
                Matchers.allOf(withId(R.id.addRestaurantFloatingActionButton),
                        childAtPosition(
                                Matchers.allOf(withId(R.id.bottom_sheet),
                                        childAtPosition(
                                                withId(R.id.design_bottom_sheet),
                                                0)),
                                1),
                        isDisplayed()));
        floatingActionButton.check(matches(isCompletelyDisplayed()));
        floatingActionButton.perform(click());
        floatingActionButton.perform(click());

        ViewInteraction linearLayout = onView(
                Matchers.allOf(withId(R.id.restaurant_details_like_button),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                1),
                        isDisplayed()));
        linearLayout.check(matches(isCompletelyDisplayed()));
        linearLayout.perform(click());
        linearLayout.perform(click());
    }



    //---------------------------------------------
    // Helper Methods
    //--------------------------------------------

    private static Boolean isCurrentUserLogged(){ return (getCurrentUser() != null); }

    @Nullable
    private static FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    private static void signInBeforeTest(){
        // Login action
        onView(withId(R.id.main_activity_button_email)).perform(click()); // Click on Gmail login btn
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Firebase Auth-UI control
        // Enter email
        ViewInteraction textInputEditText = onView(
                allOf(withId(R.id.email),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.email_layout),
                                        0),
                                0)));
        textInputEditText.perform(scrollTo(), replaceText("test@test.com"), closeSoftKeyboard());
        ViewInteraction appCompatButton2 = onView(
                Matchers.allOf(withId(R.id.button_next), withText("Next"),
                        childAtPosition(
                                Matchers.allOf(withId(R.id.email_top_layout),
                                        childAtPosition(
                                                withClassName(is("android.widget.ScrollView")),
                                                0)),
                                2)));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction textInputEditText2 = onView(
                Matchers.allOf(withId(R.id.name),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.name_layout),
                                        0),
                                0)));
        textInputEditText2.perform(scrollTo(), click());
        // Enter username
        ViewInteraction textInputEditText3 = onView(
                Matchers.allOf(withId(R.id.name),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.name_layout),
                                        0),
                                0)));
        textInputEditText3.perform(scrollTo(), replaceText("username"), closeSoftKeyboard());
        // Enter Password
        ViewInteraction textInputEditText4 = onView(
                Matchers.allOf(withId(R.id.password),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.password_layout),
                                        0),
                                0)));
        textInputEditText4.perform(scrollTo(), replaceText("123456"), closeSoftKeyboard());
        // Click Save
        ViewInteraction appCompatButton3 = onView(
                Matchers.allOf(withId(R.id.button_create), withText("Save"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void deleteTestUser(){
        // Open NavigationDrawer
        onView(withContentDescription(R.string.navigation_drawer_open)).perform(click());
        onView(withText("SETTINGS")).perform(click());
        onView(withText(R.string.profileSettings_delete_account_btn)).perform(click());
        onView(withText("YES!")).perform(click());
    }

}