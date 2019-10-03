package com.elbaz.eliran.go4lunch.views;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;

import butterknife.BindView;
import butterknife.ButterKnife;
import static android.content.ContentValues.TAG;

/**
 * Created by Eliran Elbaz on 02-Oct-19.
 */
public class RestaurantListViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.restaurantList_recyclerView_restaurantName) TextView restaurantNameTextView;
    @BindView(R.id.restaurantList_recyclerView_address) TextView addressTextView;
    @BindView(R.id.restaurantList_recyclerView_openingTimes) TextView openingTextView;
    @BindView(R.id.restaurantList_recyclerView_distance) TextView distanceTextView;
    @BindView(R.id.restaurantList_recyclerView_person) ImageView personImage;
//    @BindView(R.id.restaurantList_recyclerView_person_number) TextView personNumberTextView;
    @BindView(R.id.restaurantList_recyclerView_stars) ImageView starsImage;

    public RestaurantListViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateRestaurantsList (Result result, RequestManager glide){
        restaurantNameTextView.setText(result.getName());
        addressTextView.setText(result.getVicinity());
        openingTextView.setText(result.getOpeningHours().getOpenNow().toString());
        distanceTextView.setText(result.getId());
        Log.d(TAG, "ListView updateRestaurantsList: ");

    }


}
