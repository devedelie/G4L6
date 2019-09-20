package com.elbaz.eliran.go4lunch;

import com.elbaz.eliran.go4lunch.base.BaseActivity;

public class RestaurantsActivity extends BaseActivity {

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_restaurants;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup the toolbar
        this.configureToolbarWithDrawer();
        // setup the drawer
        this.configureDrawerLayoutAndNavigationView();
    }

}
