package com.elbaz.eliran.go4lunch.controllers.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.elbaz.eliran.go4lunch.BuildConfig;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends BaseActivity {

    // 1 - Identifier for Sign-In Activity
    private static final int RC_SIGN_IN = 100;
    private static final String PERMS_FINE = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int RC_PERMISSION_CODE = 100;
    public static Boolean mLocationPermissionGranted = false;

    @BindView(R.id.main_activity_coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.welcomeText) TextView welcomeText;
    @BindView(R.id.main_activity_button_gmail) Button gmailButtonLogin;
    @BindView(R.id.main_activity_button_facebook) Button facebookButtonLogin;

    @Override
    public int getFragmentLayout() { return R.layout.activity_main; }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "isCurrentUser: " + getCurrentUser() + " Logged? " + this.isCurrentUserLogged());
        // Verify all permissions and setups
        this.verifyPlacesSDK();
        this.isGpsEnabled();
        // Avoid login-screen if the user is already authenticated (and location services are authorized)
        if (this.isCurrentUserLogged() && mLocationPermissionGranted) {
            this.startMainRestaurantsActivity();
        }
    }

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
                        askPermission();
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
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    //-----------------End Of User's Permissions -------------------------


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.main_activity_button_gmail)
    public void onClickGmailLoginButton() {
        //Start appropriate activity
            this.startSignInActivityWithGmail();
        }

    @OnClick(R.id.main_activity_button_facebook)
    public void onClickFacebookLoginButton() {
        //Start appropriate activity
        this.startSignInActivityWithFacebook();
    }

    // --------------------
    // REST REQUEST
    // --------------------

    //  Http request that create user in firestore
    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());
        }
    }

    // --------------------
    // UI
    // --------------------

    // Show Snack Bar with a message
    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }


    //  Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                // CREATE USER IN FIRESTORE
                this.createUserInFirestore();
                showSnackBar(this.coordinatorLayout, getString(R.string.connection_succeed));
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    // --------------------
    // NAVIGATION
    // --------------------

    // Launch Sign-In Activity with Gmail- Firebase UI
    private void startSignInActivityWithGmail(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.ic_logo)
                        .build(),
                RC_SIGN_IN);
    }

    // Launch Sign-In Activity with Facebook- Firebase UI
    private void startSignInActivityWithFacebook(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.ic_logo)
                        .build(),
                RC_SIGN_IN);
    }

    // Launching Restaurants Activity
    private void startMainRestaurantsActivity(){
        if (mLocationPermissionGranted){
            Intent intent = new Intent(this, MainRestaurantActivity.class);
            startActivity(intent);
        }else {
            showSnackBar(this.coordinatorLayout, getString(R.string.need_to_authorise_location_services));
        }

    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }

}
