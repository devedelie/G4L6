package com.elbaz.eliran.go4lunch.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.GoingUserHelper;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.controllers.activities.OnNotificationClickActivity;
import com.elbaz.eliran.go4lunch.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Eliran Elbaz on 17-Oct-19.
 */
public class NotificationsService extends FirebaseMessagingService {
    private final int NOTIFICATION_ID = 007;
    private final String NOTIFICATION_TAG = "FIREBASEOC";
    private boolean mIsGoing = false;
    private String restaurantName;
    private String restaurantAddress;
    private String message;
    private List<String> workmates = new ArrayList<>();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        if(remoteMessage.getNotification() != null){
            // Get message sent by Firebase
            message = remoteMessage.getNotification().getBody();
            Log.d(TAG, "onMessageReceived: " + message);
            // Execute notification
            this.getGoingUserInfo(message);
        }
    }

    private void getGoingUserInfo(String messageBody){
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
                sendVisualNotification();
            }
        });
    }

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }


    private void sendVisualNotification() {
        // Create an Intent that will be shown when user will click on the Notification
        Intent intent = new Intent(this, OnNotificationClickActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, OnNotificationClickActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        // Create a Style for the Notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.notification_title));
        inboxStyle.addLine(message + " " + restaurantName);
        inboxStyle.addLine(restaurantAddress);
        if(workmates.size() > 0){
            inboxStyle.addLine(getString(R.string.workmates_list_for_notification));
            for(int i = 0 ; i<workmates.size() ; i++) {
                inboxStyle.addLine("- "+workmates.get(i));
            }
        }

        // Create a Channel (Android 8)
        String channelId = getString(R.string.default_notification_channel_id);

        // Build a Notification object
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_logo_pink)
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_title2))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        // Add the Notification to the Notification Manager and show it.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Message provenant de Firebase";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Show notification
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }

    //------------------------
    // Firebase Token methods
    //------------------------
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        // Add new token to SharedPreferences
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", token).apply();
    }

    public static String getTokenFromSharedPreferances(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }

    public static void getTokenFromFB(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@io.reactivex.annotations.NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = token;
                        Log.d(TAG, "The Token is: " +msg);

                    }
                });
    }

}
