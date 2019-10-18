package com.elbaz.eliran.go4lunch.views;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.models.Restaurant;
import com.elbaz.eliran.go4lunch.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;

/**
 * Created by Eliran Elbaz on 05-Oct-19.
 */
public class WorkmatesViewHolder extends RecyclerView.ViewHolder {
    // Workmates ListView items
    @BindView(R.id.workmate_detail_recyclerView_workmateImage) ImageView workmateImage;
    @BindView(R.id.workmate_detail_recyclerView_username) TextView username;
    @BindView(R.id.workmate_detail_recyclerView_midText) TextView midText;
    @BindView(R.id.workmate_detail_recyclerView_restaurantName) TextView restaurantName;

    public WorkmatesViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


    public void updateWorkmatesList (User user, String currentUserId, RequestManager glide, Resources resources){
        try{
            //set images
            glide.load(user.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(workmateImage);
            // split and get first name from the string + make first letter capital
            String getFirstName = user.getUsername();
            getFirstName = getFirstName.contains(" ") ? getFirstName.split(" ")[0] : getFirstName;
            getFirstName = getFirstName.substring(0, 1).toUpperCase() + getFirstName.substring(1);
            // set texts
            if(user.getSelectedRestaurantName().isEmpty()){
                username.setText(getFirstName);
                username.setTextColor(resources.getColor(R.color.didnot_decide_gray));
                midText.setText(R.string.did_not_decide);
                midText.setTextColor(resources.getColor(R.color.didnot_decide_gray));
            }else{
                username.setText(getFirstName);
                midText.setText(R.string.is_going_to);
                midText.setTextColor(resources.getColor(R.color.has_decided_black));
            }
            restaurantName.setText(user.getSelectedRestaurantName());
        }catch (Exception e){
            Log.d(TAG, "updateWorkmatesList: Error " + e);
        }
    }

    public void updateUsersOnRestaurantDetail(Restaurant restaurant, String currentUserId, RequestManager glide, Resources resources){
        try{
            //set images
            glide.load(restaurant.getUserGoing().getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(workmateImage);
            Log.d(TAG, "updateUsersOnRestaurantDetail: " + restaurant.getUrlImage());
            // split and get first name from the string + make first letter capital
            String getFirstName = restaurant.getUserGoing().getUsername();
            getFirstName = getFirstName.contains(" ") ? getFirstName.split(" ")[0] : getFirstName;
            getFirstName = getFirstName.substring(0, 1).toUpperCase() + getFirstName.substring(1);
            // set texts
            username.setText(getFirstName);
            midText.setText(R.string.is_joining);
        }catch (Exception e){
            Log.d(TAG, "updateWorkmatesList: Error " + e);
        }
    }
}
