package com.elbaz.eliran.go4lunch;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.elbaz.eliran.go4lunch.adapters.PageAdapter;
import com.elbaz.eliran.go4lunch.controllers.fragments.ListViewFragment;
import com.elbaz.eliran.go4lunch.controllers.fragments.MapViewFragment;
import com.elbaz.eliran.go4lunch.controllers.fragments.WorkmatesFragment;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    Context mContext;
    FragmentManager mFragmentManager;

    // Test for returning 4 tabs
    @Test
    public void PageAdapter_TestNumberOfTabs_assertCorrectNumber () throws Exception{
        // Create a new PageAdapter instance
        PageAdapter pageAdapter = new PageAdapter(mContext, mFragmentManager );
        // Check that the method returns 4 as the number of tabs to show
        assertEquals(3, pageAdapter.getCount());
    }

    //Test to verify that the fragments are not returning null
    @Test
    public void Fragment1ShouldNotBeNull() throws Exception {
        Fragment mapViewFragment = new MapViewFragment();
        assertNotNull(mapViewFragment);
    }
    @Test
    public void Fragment2ShouldNotBeNull() throws Exception {
        Fragment listViewFragment = new ListViewFragment();
        assertNotNull(listViewFragment);
    }
    @Test
    public void Fragment3ShouldNotBeNull() throws Exception {
        Fragment workmatesFragment = new WorkmatesFragment();
        assertNotNull(workmatesFragment);
    }

}