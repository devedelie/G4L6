package com.elbaz.eliran.go4lunch;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.elbaz.eliran.go4lunch.adapters.PageAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

public class MainRestaurantActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected TabLayout tabs;
    private Toolbar toolbar;
    protected Context mContext;
    View rootView;
    private int[] tabIcons = {R.drawable.ic_mapview_icon, R.drawable.ic_listview_icon, R.drawable.ic_workmates_icon};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_restaurant);
        // Context for the fragments
        mContext = this;
        // Get RootView for snackBarMessage
        rootView = getWindow().getDecorView().getRootView();
        // Configure the basic design structure of the app with tabs and viewPager
        this.configureViewPagerAndTabs();
        this.configureToolbarWithDrawer();
        this.configureDrawerLayoutAndNavigationView();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    // --------------------
    // UI
    // --------------------
    // Multifunction Toolbar with drawer and search
    protected void configureToolbarWithDrawer(){
        // Get the toolbar view inside the activity layout
        this.toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar
        setSupportActionBar(toolbar);
    }

    /**
     * Navigation drawer config
     */
    protected void configureDrawerLayoutAndNavigationView(){
        // Configure drawer layout
        this.drawerLayout = findViewById(R.id.main_restaurant_activity_drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // Configure NavigationView & set item selection listener
        this.navigationView = findViewById(R.id.drawer_restaurant_main_activity);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * 2 - ViewPager configuration + Tab Layout
     */
    protected void configureViewPagerAndTabs(){
        //Get ViewPager from layout
        ViewPager pager = findViewById(R.id.activity_main_restaurant_viewpager);
        //Set Adapter PageAdapter and glue it together
        pager.setAdapter(new PageAdapter(mContext, getSupportFragmentManager()));
        // Set the offscreenLimit - loads 2 fragments simultaneously offScreen, to improves fluency of visual load
        pager.setOffscreenPageLimit(2);

        //Get TabLayout from layout
        tabs= findViewById(R.id.activity_main_restaurant_tabs);
        //Glue TabLayout and ViewPager together
        tabs.setupWithViewPager(pager);
        //Design purpose. Tabs have the same width
        tabs.setTabMode(TabLayout.MODE_FIXED);
        setupTabIcons();
    }

    // Set Icons for tabs
    private void setupTabIcons() {
        tabs.getTabAt(0).setIcon(tabIcons[0]);
        tabs.getTabAt(1).setIcon(tabIcons[1]);
        tabs.getTabAt(2).setIcon(tabIcons[2]);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
