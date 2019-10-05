package com.elbaz.eliran.go4lunch.controllers.activities;

import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.elbaz.eliran.go4lunch.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import butterknife.BindView;

import static android.content.ContentValues.TAG;

public class YourLunchActivity extends BaseActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.your_lunch_activity_text) TextView mTextView;
    @BindView(R.id.your_lunch_activity_restaurant_name) TextView restaurantName;

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_your_lunch;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.configureToolbarWithBackArrow();
        this.setUIElements();
    }

    // General Toolbar for side activities with back arrow only
    protected void configureToolbarWithBackArrow(){
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
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

    public void setUIElements(){
        mTextView.setText(R.string.you_are_going_to);
        //  Get additional data from Firestore (restaurant name & isGoing
        UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);
                restaurantName.setText(currentUser.getSelectedRestaurantName());
                Log.d(TAG, "setUIElements: restoName: " + currentUser.getSelectedRestaurantName());

            }
        });
    }
}
