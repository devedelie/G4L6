package com.elbaz.eliran.go4lunch.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.GoingUserHelper;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.elbaz.eliran.go4lunch.controllers.activities.MainActivity;
import com.elbaz.eliran.go4lunch.models.User;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.ContentValues.TAG;

public class ProfileSettingsActivity extends BaseActivity {
    @BindView(R.id.profile_activity_edit_text_username) TextInputEditText userNameEditText;
    @BindView(R.id.profile_activity_text_view_email) TextView emailText;
    @BindView(R.id.profile_activity_button_sign_out) Button signoutBtn;
    @BindView(R.id.profile_activity_button_delete) Button deleteBtn;
    @BindView(R.id.profile_activity_check_btn_update_name) ImageButton checkBtn;
    @BindView(R.id.profile_activity_user_image) ImageView imageViewProfile;
    @BindView(R.id.toolbar) Toolbar toolbar;
    // Identify each Http Request
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;
    private SharedViewModel sharedViewModel;
    private String restaurantToDelete;


    @Override
    public int getFragmentLayout() {
        return R.layout.activity_profile_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.configureToolbarWithBackArrow();
        this.updateUIOncreate();
    }

    // General Toolbar for side activities with back arrow only
    protected void configureToolbarWithBackArrow(){
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void updateUIOncreate(){
        if(this.getCurrentUser() != null){

            if(this.getCurrentUser().getPhotoUrl() != null){
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProfile);
            }
            // Get email
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            this.emailText.setText(email);
            // Get username from Firestore
            UserHelper.getUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    String username = TextUtils.isEmpty(currentUser.getUsername()) ? getString(R.string.info_no_username_found) : currentUser.getUsername();
                    Log.d(TAG, "onSuccess: "+ username);
                    userNameEditText.setText(username);
                    restaurantToDelete =currentUser.getSelectedRestaurantName();
                }
            });
        }
    }

    // Detect the click on toolbar's "back" button and finish the current activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ( id == android.R.id.home ) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.profile_activity_check_btn_update_name)
    public void onClickUpdateUsername(){ this.updateUsernameInFirebase(); }

    @OnClick(R.id.profile_activity_button_sign_out)
    public void onClickSignoutBtn(){ this.signOutUserFromFirebase();}

    @OnClick(R.id.profile_activity_button_delete)
    public void onClickDeleteButton() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_choice_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteUserFromFirebase();
                    }
                })
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();
    }

    // --------------------
    // REST REQUESTS --  Create http requests (Update, SignOut & Delete)
    // --------------------

    private void updateUsernameInFirebase(){
        String username = this.userNameEditText.getText().toString();
        if (this.getCurrentUser() != null){
            if (!username.isEmpty() &&  !username.equals(getString(R.string.info_no_username_found))){
                UserHelper.updateUsername(username, this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));
            }
        }
    }

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private void deleteUserFromFirebase(){
        Log.d(TAG, "deleteUserFromFirebase: ");
        if (this.getCurrentUser() != null) {
            // Delete from goingUsers list if any value of the current user exist
            if(restaurantToDelete != null && !restaurantToDelete.isEmpty()){
                GoingUserHelper.deleteUserFromGoingListAfterAccountDelete(restaurantToDelete, getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
            }
            // Delete user from Firestore
            UserHelper.deleteUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
            // Delete user from Firebase
            AuthUI.getInstance()
                    .delete(this).addOnFailureListener(this.onFailureListener())
                    .addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    // 8 - Hiding Progress bar after request completed
                    case UPDATE_USERNAME:
                        updateName();
                        break;
                    case SIGN_OUT_TASK:
                        clearActivityBackStack();
                        break;
                    case DELETE_USER_TASK:
                        Log.d(TAG, "onSuccess: updateUIAfterRESTRequestsCompleted");
                        clearActivityBackStack();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void updateName(){
        Toast toast = Toast.makeText(this,getString(R.string.name_successfully_updated), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void clearActivityBackStack(){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |  Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
//
//    private void deleteUser(){
//        Log.d(TAG, "deleteUser: ");
//        finishAffinity();
//    }

}


