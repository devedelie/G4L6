package com.elbaz.eliran.go4lunch.views;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.elbaz.eliran.go4lunch.BuildConfig;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.GOOGLE_MAPS_API_BASE_URL;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE_KEY;

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
    @BindView(R.id.restaurantList_recyclerView_restaurantImage) ImageView restaurantImage;

    public RestaurantListViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateRestaurantsList (Result result, RequestManager glide){
        try{
            //set images
            String imageUrl = GOOGLE_MAPS_API_BASE_URL + URL_FOR_IMAGE + result.getPhotos().get(0).getPhotoReference() + URL_FOR_IMAGE_KEY + BuildConfig.GOOGLE_BROWSER_API_KEY;
            Log.d(TAG, "setViewElementsForNearbyPlaces: " + imageUrl);
            glide.load(imageUrl).apply(RequestOptions.centerCropTransform()).into(restaurantImage);
            // set texts
            restaurantNameTextView.setText(result.getName());
            addressTextView.setText(result.getVicinity());
            openingTextView.setText(result.getOpeningHours().getOpenNow().toString());
            distanceTextView.setText(result.getRating().toString());
            Log.d(TAG, "ListView updateRestaurantsList: ");
        }catch (Exception e){
            Log.d(TAG, "updateRestaurantsList: Error " + e);
        }


    }


}
