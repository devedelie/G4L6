package com.elbaz.eliran.go4lunch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.Nullable;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.elbaz.eliran.go4lunch.controllers.activities.MainActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by Eliran Elbaz on 24-Oct-19.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityCreateTestUser {
    private static Context appContext= getInstrumentation().getTargetContext();
    // --------------------------------------Annotation Tags Setup----------------------------------
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void beforeclass(){
        System.out.println("@beforeclass");
        // Logout if user is connected to Firebase to enable Tests in MainActivity.java
                if(isCurrentUserLogged()){
            AuthUI.getInstance()
                    .signOut(appContext);
        }
    }

    // Configurations and actions to be taken before each test
    @Before
    public void setUp() throws Exception{ }

    // Configurations and actions to be taken after each test (ex: close the app)
    @After
    public void  tearDown() throws Exception{}

    //---------------------------------------------------------------------------------------------

    @Test
    public void mainActivityRegisterUserWithEmail_onSuccessToRegister_shouldDeleteTestAccount() throws Exception{
        // Verify view elements
        onView(withId(R.id.main_activity_coordinator_layout)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.welcomeText)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.main_activity_button_email)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.main_activity_button_gmail)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.main_activity_button_facebook)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.main_activity_button_twitter)).check(matches(isCompletelyDisplayed()));
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

        // Open NavigationDrawer
        onView(withContentDescription(R.string.navigation_drawer_open)).perform(click());
        onView(withText("SETTINGS")).perform(click());
        onView(withText(R.string.profileSettings_delete_account_btn)).perform(click());
        onView(withText("YES!")).perform(click());
    }

    //--------------------
    // Helpers
    //---------------------

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
}
