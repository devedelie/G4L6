package com.elbaz.eliran.go4lunch;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    // 1 - Identifier for Sign-In Activity
    private static final int RC_SIGN_IN = 100;

    @BindView(R.id.main_activity_coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.welcomeText) TextView welcomeText;
    @BindView(R.id.main_activity_button_login) Button buttonLogin;

    @Override
    public int getFragmentLayout() { return R.layout.activity_main; }

    @Override
    protected void onResume() {
        super.onResume();
        // Avoid login screen if user is already authenticated
        if (this.isCurrentUserLogged()) {
            this.startRestaurantsActivity();
        }
    }



    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.main_activity_button_login)
    public void onClickLoginButton() {
        //Start appropriate activity
            this.startSignInActivity();
        }


    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    protected Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }

    // --------------------
    // NAVIGATION
    // --------------------

    // 2 - Launch Sign-In Activity - Firebase UI (not in layouts xmls)
    private void startSignInActivity(){
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

    // Launching Restaurants Activity
    private void startRestaurantsActivity(){
        Intent intent = new Intent(this, RestaurantsActivity.class);
        startActivity(intent);
    }

}
