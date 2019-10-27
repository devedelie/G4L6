package com.elbaz.eliran.go4lunch.controllers.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.GoingUserHelper;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.elbaz.eliran.go4lunch.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OnNotificationClickActivity extends BaseActivity {
    private boolean mIsGoing = false;
    private String restaurantName;
    private String restaurantAddress;
    private List<String> workmates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getGoingUserInfo();
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_on_notification_click;
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
        TextView dialogRestaurantGoingUsers = (TextView) dialogView.findViewById(R.id.dialog_content_restaurant_goingUsers);
        TextView dialogRestaurantGoingUsersList = (TextView) dialogView.findViewById(R.id.dialog_content_restaurant_goingUsersList);
        TextView dialogBottomText = (TextView) dialogView.findViewById(R.id.dialog_bottom_text);
        Button dialogButton = (Button) dialogView.findViewById(R.id.dialog_button);

        dialogContent.setText(getResources().getString(R.string.dialog_content));
        dialogRestaurantName.setText(restaurantName);
        dialogRestaurantAddress.setText(restaurantAddress);
        dialogRestaurantGoingUsers.setText(getString(R.string.dialog_joining_workmates));
        if (workmates != null){
            dialogRestaurantGoingUsersList.setText(workmates.toString());
        }
        dialogRestaurantName.setPaintFlags(dialogRestaurantName.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        dialogRestaurantGoingUsers.setPaintFlags(dialogRestaurantGoingUsers.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        dialogBottomText.setText(getResources().getString(R.string.dialog_bon_appetit));


        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
                intentApp();
            }
        });
    }

    private void intentApp(){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void getGoingUserInfo(){
        // Get user 'isGoing' boolean from Firestore
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);
                // If user 'isGoing', invoke the next method
                mIsGoing = currentUser.getIsGoing();
                restaurantName = currentUser.getSelectedRestaurantName();
                restaurantAddress = currentUser.getSelectedRestaurantAddress();
                if(mIsGoing){
                    getGoingWorkmates();
                    // set the value back to false
                    mIsGoing=false;
                }
            }
        });
    }

    private void getGoingWorkmates(){
        // Get the list of workmates who selected the same restaurant
        GoingUserHelper.getAllRestaurantsWithGoingUsers(restaurantName).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.size() > 0){
                    // Get all going users and add to ArrayList
                    for(int i = 0 ; i<queryDocumentSnapshots.size() ; i++){
                        String username = queryDocumentSnapshots.getDocuments().get(i).getData().get("userGoing").toString();
                        username = username.substring(username.indexOf("username")+9);
                        username = username.substring(0, username.length() - 1);
                        username = username.substring(0, 1).toUpperCase() + username.substring(1);
                        workmates.add(username);
                    }
                }
                yourLunchDialog();
            }
        });
    }
}
