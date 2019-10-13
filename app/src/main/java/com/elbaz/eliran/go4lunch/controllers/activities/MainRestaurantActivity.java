package com.elbaz.eliran.go4lunch.controllers.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.elbaz.eliran.go4lunch.BuildConfig;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.adapters.PageAdapter;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.auth.ProfileSettingsActivity;
import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.elbaz.eliran.go4lunch.models.User;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainRestaurantActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.activity_main_bottom_navigation) BottomNavigationView bottomNavigationView;
    @BindView(R.id.activity_main_restaurant_viewpager) ViewPager pager;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.main_restaurant_activity_drawerLayout) DrawerLayout drawerLayout;
    @BindView(R.id.drawer_restaurant_main_activity) NavigationView navigationView;
    View rootView;
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    // Identify each Http Request
    private static final int SIGN_OUT_TASK = 10;
    private static final String PERMS_FINE = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int RC_PERMISSION_CODE = 100;
    public static Boolean mLocationPermissionGranted = false;
    public Context mContext;
    SharedViewModel mSharedViewModel;
    // AutoComplete search
    private List<Place.Field> mFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG); // Set the fields to specify which types of place data to return after the user has made a selection.
    private String textForDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        // Verify all permissions and setups
        this.verifyPlacesSDK();
        this.isGpsEnabled();
        // Context for the fragments
        mContext = this;
        // Get RootView for snackBarMessage
        rootView = getWindow().getDecorView().getRootView();
        // Configure the basic design structure of the app with tabs and viewPager
        this.configureViewPagerAndTabs();
        this.configureToolbarWithDrawer();
        this.configureDrawerLayoutAndNavigationView();
        // Assign a ViewModel
        mSharedViewModel = ViewModelProviders.of(this).get(SharedViewModel.class);
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_main_restaurant; }

    //----------------------------
    // Permissions & device setup
    //----------------------------

    public void verifyPlacesSDK(){
        // Verify OR Initialize "Places SDK" on the device
        if (!Places.isInitialized()) {
            // Initialize Places.
            Places.initialize(getApplicationContext(), BuildConfig.GOOGLE_API_KEY);
            // Create a new Places client instance.
            PlacesClient placesClient = Places.createClient(this);
        }
    }

    public void isGpsEnabled(){
        final LocationManager manager = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }else {
            askPermission();
        }

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                        //
                        askPermission();
//                        initMap();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Method to ask the user for location authorization (with EasyPermissions support)
     */
    private void askPermission() {
        if (!EasyPermissions.hasPermissions(this, PERMS_FINE )) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access), RC_PERMISSION_CODE, PERMS_FINE);
            return;
        }
        mLocationPermissionGranted = true;
        Toast.makeText(this, "Location access authorized!", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 2 - Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    //-----------------End Of User's Permissions -------------------------

    //-------------------
    // UI Configuration
    //-------------------

    // Toolbar for Navigation Drawer and search icon
    protected void configureToolbarWithDrawer(){
        // Sets the Toolbar
        setSupportActionBar(toolbar);
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        if(pager.getCurrentItem() == 1){
//        MenuItem item = menu.findItem(R.id.menu_sort_icon);
//        item.setVisible(true);
//        }
//        if(pager.getCurrentItem() ==  2){
//            MenuItem item = menu.findItem(R.id.menu_search_icon);
//            item.setVisible(false);
//        }
//        return true;
//    }

    /**
     * ViewPager configuration + Tab Layout
     */
    protected void configureViewPagerAndTabs(){
        //Set Adapter PageAdapter and glue it together
        pager.setAdapter(new PageAdapter(mContext, getSupportFragmentManager()));
        // Set the offscreenLimit - loads 2 fragments simultaneously offScreen, to improves fluency of visual load
        pager.setOffscreenPageLimit(2);
        // Disable ViewPager horizontal switch
        pager.beginFakeDrag();

        // Configure BottomView Listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_mapView:
//                        configureToolbarWithDrawer();
                        pager.setCurrentItem(0);
                        break;
                    case R.id.action_listView:
//                        configureToolbarWithDrawer(); // Add 'sort' icon in ListViewFragment
                        pager.setCurrentItem(1);
                        break;
                    case R.id.action_workmates:
//                        configureToolbarWithDrawer(); // Hide 'Search' icon in Workmates fragment
                        pager.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Inflate the top-menu (menu with search and parameters icons)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu and add it to the Toolbar
        getMenuInflater().inflate(R.menu.menu_activity_main_restaurant, menu);

        return true;
    }

    // OptionMenu item selection (Search places auto-complete)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_search_icon){
            setCurrentPagerToViewModel(pager.getCurrentItem());
            this.launchAutocompleteSearchBar();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Navigation drawer config
     */
    protected void configureDrawerLayoutAndNavigationView(){
        // Configure drawer layout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Configure NavigationView & set item selection listener
        View headerView = this.navigationView.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.navigation_header_name);
        TextView userEmail = headerView.findViewById(R.id.navigation_header_email);
        ImageView userImage = headerView.findViewById(R.id.navigation_header_image);
        // set user name and email
        userName.setText(this.getCurrentUser().getDisplayName());
        userEmail.setText(this.getCurrentUser().getEmail());
        // Set Image
        if (this.getCurrentUser() != null) {
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(userImage);
            }
        }
        // Set listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Drawer item selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int order = menuItem.getOrder();
        Log.d(TAG, "Test onNavigationItemSelected: "+ order);
        switch (order){
            case 0:
                // Your lunch action
                this.yourLunchDialog();
                break;
            case 1:
                // settings action
                this.goToProfileSettings();
                break;
            case 2:
                // logout action
                this.signOutUserFromFirebase();
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void goToProfileSettings(){
        Intent intent = new Intent(this, ProfileSettingsActivity.class);
        startActivity(intent);
    }

    private void yourLunchDialog(){
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_your_lunch, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Set textViews
        TextView dialogContent = (TextView) dialogView.findViewById(R.id.dialog_content_text);
        TextView dialogRestaurantName = (TextView) dialogView.findViewById(R.id.dialog_content_restaurant_name);
        TextView dialogBottomText = (TextView) dialogView.findViewById(R.id.dialog_bottom_text);

        String restaurantName = getTextForDialog();
        if(restaurantName != null && !restaurantName.isEmpty()){
            dialogContent.setText(getResources().getString(R.string.dialog_content));
            dialogRestaurantName.setText(restaurantName);
            dialogBottomText.setText(getResources().getString(R.string.dialog_bon_appetit));
        }else{
            dialogContent.setText(getResources().getString(R.string.dialog_content_no_go));
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

//    private void goToYourLunchActivity(){
//        Intent intent = new Intent(this, YourLunchActivity.class);
//        startActivity(intent);
//    }

    // Send data to ViewModel - LiveData
    private void setCurrentPagerToViewModel(Integer pagerCurrentItem){
        mSharedViewModel.setPagerCurrentItem(pagerCurrentItem);
    }

    private void launchAutocompleteSearchBar(){
        // Bias results to Paris region (use 'bounds' variable in below filter)
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(48.832304, 2.239726),
                new LatLng(48.900962, 2.42124));

        // Start the autocomplete intent. (OVERLAY + ESTABLISHMENT + FR)
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, mFields)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setCountry("FR")
                .setLocationBias(bounds)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        Log.d(TAG, "onOptionsItemSelected: check");
    }

    // onActivityResult for Search Auto-Complete
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: code is " + requestCode +" "+ resultCode);
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.d(TAG, "onActivityResult: current item" + pager.getCurrentItem());
                mSharedViewModel.setSearchObject(place);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, "onActivityResult Error: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    // --------------------
    // REST REQUESTS
    // --------------------
    // 1 - Create http requests (SignOut & Delete)

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(origin == SIGN_OUT_TASK)
                    finish();
            }
        };
    }

    public String getTextForDialog(){

        UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);
                textForDialog = currentUser.getSelectedRestaurantName();
                Log.d(TAG, "setUIElements: restoName: " + currentUser.getSelectedRestaurantName());
            }
        });
        return textForDialog;
    }

}
