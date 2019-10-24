package com.elbaz.eliran.go4lunch;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.elbaz.eliran.go4lunch.controllers.activities.MainActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

/**
 * Created by Eliran Elbaz on 24-Oct-19.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
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
    public void useAppContext() {
        // Context of the app under test.
        Assert.assertEquals("com.elbaz.eliran.go4lunch", appContext.getPackageName());
    }

    @Test
    public void SearchActivity_runSearchWithSelectedCategory_returnTenResults() throws Exception{
        // Verify view elements
        onView(withId(R.id.main_activity_coordinator_layout)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.welcomeText)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.main_activity_button_gmail)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.main_activity_button_facebook)).check(matches(isCompletelyDisplayed()));
        // Login action
//        onView(withId(R.id.main_activity_button_gmail)).perform(click()); // Click on Gmail login btn
//
    }

    //--------------------
    // Helpers
    //---------------------

    private static Boolean isCurrentUserLogged(){ return (getCurrentUser() != null); }

    @Nullable
    private static FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

}
