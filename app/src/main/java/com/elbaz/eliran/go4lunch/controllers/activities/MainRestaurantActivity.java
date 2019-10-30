package com.elbaz.eliran.go4lunch.controllers.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.adapters.PageAdapter;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.auth.ProfileSettingsActivity;
import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.elbaz.eliran.go4lunch.controllers.fragments.ListViewFragment;
import com.elbaz.eliran.go4lunch.controllers.fragments.MapViewFragment;
import com.elbaz.eliran.go4lunch.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
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

import static android.content.ContentValues.TAG;

public class MainRestaurantActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener, Spinner.OnItemSelectedListener{
    @BindView(R.id.activity_main_bottom_navigation) BottomNavigationView bottomNavigationView;
    @BindView(R.id.activity_main_restaurant_viewpager) ViewPager pager;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.main_restaurant_activity_drawerLayout) DrawerLayout drawerLayout;
    @BindView(R.id.drawer_restaurant_main_activity) NavigationView navigationView;
    View rootView;
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    // Identify each Http Request
    private static final int SIGN_OUT_TASK = 10;
    public Context mContext;
    // AutoComplete searchAction
    private List<Place.Field> mFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG); // Set the fields to specify which types of place data to return after the user has made a selection.
    public static String dialogRestaurantName, dialogAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        //---Firebase Cloud-Messaging Token------
//        NotificationsService.getTokenFromFB();
        //---------------------------------
        mContext = this;
        // Get RootView for snackBarMessage
        rootView = getWindow().getDecorView().getRootView();
        // Check network connectivity
        if(!this.isNetworkAvailable()){
            displayMobileDataSettingsDialog(this, this);
        }else{
            // Configure the basic design structure of the app with tabs and viewPager
            this.configureViewPager();
            this.configureBottomNavigation();
            this.configureToolbarWithDrawer();
            this.configureDrawerLayoutAndNavigationView();
        }
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_main_restaurant; }

    @Override
    public void onBackPressed() {
        // Handle 'back' press to close menu
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            this.moveTaskToBack(true);
        }
    }

    //-------------------
    // UI Configuration
    //-------------------

    //ViewPager configuration + BottomNavigation Layout
    protected void configureViewPager() {
        //Set Adapter PageAdapter and glue it together
        pager.setAdapter(new PageAdapter(mContext, getSupportFragmentManager()));
        // Set the offscreenLimit - loads 2 fragments simultaneously offScreen, to improves fluency of visual load
        pager.setOffscreenPageLimit(2);
        // ViewPager scroll listener
        pager.addOnPageChangeListener(this);
    }

     private void configureBottomNavigation(){
        // Configure BottomNavigation Listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_mapView:
                        pager.setCurrentItem(0);
                        break;
                    case R.id.action_listView:
                        pager.setCurrentItem(1);
                        break;
                    case R.id.action_workmates:
                        pager.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });
    }

    // Toolbar for Navigation Drawer and searchAction icon
    protected void configureToolbarWithDrawer(){
        setSupportActionBar(toolbar);
    }

     // Inflate the top-menu (menu with searchAction and parameters icons)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu and add it to the Toolbar
        getMenuInflater().inflate(R.menu.menu_activity_main_restaurant, menu);
        // Configure Spinner
        MenuItem item = menu.findItem(R.id.menu_sort_icon);
        Spinner spinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        return true;
    }

    // OptionMenu item selection (Search places auto-complete)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_search_icon){
            if(pager.getCurrentItem() == 0 || pager.getCurrentItem()== 1){
                launchAutocompleteSearchBar(); // Launch Autocomplete searchAction bar only for fragment 0/1
            }else if(pager.getCurrentItem() == 2){
                // Search for workmates (Not implemented yet)
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // Navigation drawer config
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

    //-------------------
    // Actions
    //-------------------

    // Drawer item selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int order = menuItem.getOrder();
        Log.d(TAG, "Test onNavigationItemSelected: "+ order);
        switch (order){
            case 0:
                this.getUserDataFromFirestore(); // Your lunch action
                break;
            case 1:
                this.goToProfileSettings(); // settings action
                break;
            case 2:
                this.signOutUserFromFirebase(); // logout action
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
        TextView dialogRestaurantAddress = (TextView) dialogView.findViewById(R.id.dialog_content_restaurant_address);
        TextView dialogBottomText = (TextView) dialogView.findViewById(R.id.dialog_bottom_text);
        Button dialogButton = (Button) dialogView.findViewById(R.id.dialog_button);
        if(MainRestaurantActivity.dialogRestaurantName != null && !MainRestaurantActivity.dialogRestaurantName.isEmpty()){
            dialogContent.setText(getResources().getString(R.string.dialog_content));
            dialogRestaurantName.setText(MainRestaurantActivity.dialogRestaurantName);
            dialogRestaurantAddress.setText(dialogAddress);
            dialogRestaurantName.setPaintFlags(dialogRestaurantName.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
            dialogBottomText.setText(getResources().getString(R.string.dialog_bon_appetit));
        }else{
            dialogContent.setText(getResources().getString(R.string.dialog_content_no_go));
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
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
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                searchAction(place); // Pass the data to current fragment
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, "onActivityResult Error: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    // Search action to be taken for MapView and ListView Fragments
    private void searchAction(Place place){
        switch (pager.getCurrentItem()){
            case 0:
                MapViewFragment mapViewFragment = (MapViewFragment) pager.getAdapter().instantiateItem(pager, pager.getCurrentItem());
                mapViewFragment.searchAction(place);
                break;
            case 1:
                ListViewFragment listViewFragment = (ListViewFragment) pager.getAdapter().instantiateItem(pager, pager.getCurrentItem());
                listViewFragment.searchAction(place);
                break;
        }
    }

    // Spinner Item selection
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if(!parent.getItemAtPosition(pos).toString().equals("Default")){
            Log.d(TAG, "onItemSelected Sort: "+ parent.getItemAtPosition(pos).toString());
            ListViewFragment listViewFragment = (ListViewFragment) pager.getAdapter().instantiateItem(pager, pager.getCurrentItem());
            listViewFragment.sortResults(parent.getItemAtPosition(pos).toString());
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    // --------------------
    // REST REQUESTS
    // --------------------

    // Create http requests (SignOut & Delete)
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
                logOut();
            }
        };
    }

    public void getUserDataFromFirestore(){
        UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);
                dialogRestaurantName = currentUser.getSelectedRestaurantName();
                dialogAddress = currentUser.getSelectedRestaurantAddress();
                yourLunchDialog();
            }
        });
    }

    private void logOut(){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |  Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // ------------------
    // ViewPager helper
    // ------------------
    // The below Override method will 'setChecked' the correct BottomView element to flow with viewPager scroll
    @Override public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: " + position);
        bottomNavigationView.getMenu().getItem(position).setChecked(true);
    }
    @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
    @Override public void onPageScrollStateChanged(int state) { }

}
