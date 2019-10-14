package com.elbaz.eliran.go4lunch.views;

import android.graphics.Typeface;
import android.location.Location;
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
import com.elbaz.eliran.go4lunch.api.GoingUserHelper;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.controllers.fragments.MapViewFragment.deviceLocation;
import static com.elbaz.eliran.go4lunch.models.Constants.GOOGLE_MAPS_API_BASE_URL;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE_KEY;

/**
 * Created by Eliran Elbaz on 02-Oct-19.
 */
public class RestaurantListViewHolder extends RecyclerView.ViewHolder {
    // Restaurant ListView items
    @BindView(R.id.restaurantList_recyclerView_restaurantName) TextView restaurantNameTextView;
    @BindView(R.id.restaurantList_recyclerView_address) TextView addressTextView;
    @BindView(R.id.restaurantList_recyclerView_openingTimes) TextView openingTextView;
    @BindView(R.id.restaurantList_recyclerView_distance) TextView distanceTextView;
    @BindView(R.id.restaurantList_recyclerView_person) ImageView personImage;
    @BindView(R.id.restaurantList_recyclerView_persons_number) TextView personNumberTextView;
    @BindView(R.id.restaurantList_recyclerViewList_stars_1) ImageView starsImage;
    @BindView(R.id.restaurantList_recyclerViewList_stars_2) ImageView starsImage2;
    @BindView(R.id.restaurantList_recyclerViewList_stars_3) ImageView starsImage3;
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
            restaurantNameTextView.setTypeface(null, Typeface.BOLD);
            addressTextView.setText(result.getVicinity());
            openingTextView.setText(result.getOpeningHours().getOpenNow() ? R.string.listView_open_now : R.string.listView_closed);
            openingTextView.setTypeface(null, Typeface.ITALIC);
            distanceTextView.setText(calculateDistance(result)+"m");
            retrieveGoingPersons(result);
            calculateStarRating(result);
            Log.d(TAG, "ListView updateRestaurantsList: ");
        }catch (Exception e){
            Log.d(TAG, "updateRestaurantsList: Error " + e);
        }
    }

    private int calculateDistance(Result result){
        float[] distance = new float[1];
        try{
            Location.distanceBetween(deviceLocation.getLatitude(), deviceLocation.getLongitude(), result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng(), distance);
        }catch (Exception e){
            Log.d(TAG, "calculateDistance Error: " +e);
        }
        return Math.round(distance[0]);
    }

    private void calculateStarRating(Result result){
        try{
            Log.d(TAG, "calculateStarRating: "+ result.getName()+ " " +  result.getRating());
            double ratingValue = result.getRating(); // round the value of rating
            if (ratingValue < 1.25 ) {
                starsImage.setVisibility(View.INVISIBLE);
                starsImage2.setVisibility(View.INVISIBLE);
                starsImage3.setVisibility(View.INVISIBLE);
            }else if(ratingValue >= 1.25 && ratingValue < 2.5){
                starsImage.setVisibility(View.VISIBLE);
                starsImage2.setVisibility(View.INVISIBLE);
                starsImage3.setVisibility(View.INVISIBLE);
            }else if(ratingValue >= 2.5 && ratingValue < 3.75){
                starsImage.setVisibility(View.VISIBLE);
                starsImage2.setVisibility(View.VISIBLE);
                starsImage3.setVisibility(View.INVISIBLE);
            }else if(ratingValue >= 3.75 && ratingValue <= 5){
                starsImage.setVisibility(View.VISIBLE);
                starsImage2.setVisibility(View.VISIBLE);
                starsImage3.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            Log.d(TAG, "calculateStarRating: "+ e);
        }
    }

    private void retrieveGoingPersons(Result result){
            GoingUserHelper.getAllRestaurantsWithGoingUsers(result.getName()).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    Log.d(TAG, "#of persons: "+ queryDocumentSnapshots.size());
                    if(queryDocumentSnapshots.size() > 0){
                        personNumberTextView.setVisibility(View.VISIBLE);
                        personNumberTextView.setText("("+queryDocumentSnapshots.size()+")");
                        personImage.setVisibility(View.VISIBLE);
                    }

                }
            });
        }

}



