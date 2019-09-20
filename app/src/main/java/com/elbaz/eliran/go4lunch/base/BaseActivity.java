package com.elbaz.eliran.go4lunch.base;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.elbaz.eliran.go4lunch.R;
import com.google.android.material.navigation.NavigationView;

import butterknife.ButterKnife;

/**
 * Created by Eliran Elbaz on 19-Sep-19.
 */
public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public Toolbar toolbar;
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;

    // --------------------
    // LIFE CYCLE
    // --------------------

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(this.getFragmentLayout());
        ButterKnife.bind(this); //Configure Butterknife
    }

    public abstract int getFragmentLayout();

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

    // General Toolbar for side activities with back arrow only
    protected void configureToolbarWithBackArrow(){
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Navigation drawer config
     */
    protected void configureDrawerLayoutAndNavigationView(){
        // Configure drawer layout
        this.drawerLayout = findViewById(R.id.activity_restaurant_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // Configure NavigationView & set item selection listener
        this.navigationView = findViewById(R.id.drawer_restaurant_activity);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Item Selection listener for navigation drawer
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return true;
    }

}
