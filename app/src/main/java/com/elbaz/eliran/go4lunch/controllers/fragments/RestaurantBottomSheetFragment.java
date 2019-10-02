package com.elbaz.eliran.go4lunch.controllers.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elbaz.eliran.go4lunch.BuildConfig;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.models.SearchAuto;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.GOOGLE_MAPS_API_BASE_URL;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE_KEY;

/**
 * Created by Eliran Elbaz on 29-Sep-19.
 */
public class RestaurantBottomSheetFragment extends BottomSheetDialogFragment {
    // FOR DESIGN
    @BindView(R.id.fragment_bottomsheet_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fragment_detail_image) ImageView fragmentDetailMainImage;
    @BindView(R.id.fragment_restaurant_detail_title) TextView restaurantDetailTitle;
    @BindView(R.id.fragment_restaurant_detail_address) TextView restaurantDetailAddress;
    @BindView(R.id.fragment_restaurant_detail_description) TextView restaurantDetailDescription;
    @BindView(R.id.detail_restaurant_likes) TextView restaurantDetailLikes;
    private SharedViewModel mSharedViewModel;
    private List<Result> mResults;
    public SearchAuto searchAuto;
    private List<SearchAuto> mSearchAutosArray;
    private static final String MARKER_TAG = "MARKER_TAG";
    PlacesClient mPlacesClient;

    public static RestaurantBottomSheetFragment newInstance(int markerTag) {
        RestaurantBottomSheetFragment restaurantBottomSheetFragment;
        restaurantBottomSheetFragment = new RestaurantBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(MARKER_TAG, markerTag);
        restaurantBottomSheetFragment.setArguments(bundle);
        return restaurantBottomSheetFragment ;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_details
                , container, false);
        ButterKnife.bind(this, view);
        mPlacesClient = Places.createClient(getActivity());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set ViewModel Elements under onActivityCreated() to scope it to the lifeCycle of the Fragment
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        // Observe fetched Results
        mSharedViewModel.getResults().observe(getViewLifecycleOwner(), new Observer<List<Result>>() {
            @Override
            public void onChanged(List<Result> results) {
                // set results
                mResults = new ArrayList<>();
                mResults.clear();
                mResults.addAll(results);
            }
        });
        // Observe Search ArrayList
        mSharedViewModel.getSearchArray().observe(getViewLifecycleOwner(), new Observer<List<SearchAuto>>() {
            @Override
            public void onChanged(List<SearchAuto> searchAutos) {
                // update the array with new added objects
                mSearchAutosArray =null;
                mSearchAutosArray = new ArrayList<>();
                mSearchAutosArray = searchAutos;
                Log.d(TAG, "Array onChanged: " + mSearchAutosArray.get(0).getName());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setViewElements();
    }

    private void setViewElements(){
        int i = getArguments().getInt(MARKER_TAG);
        if (i >=0 && i<20){
            setViewElementsForNearbyPlacesSheet(i);
        }else if (i>= 100){
            setViewElementsForAutoCompleteSheet(i);
        }
    }

    private void setViewElementsForNearbyPlacesSheet(int index){
        Log.d(TAG, "TagNumber check "+index + "photo ref: "+ mResults.get(index).getPhotos().get(0).getPhotoReference());
        // Set Image referance string and set image with Glide
        String imageUrl = GOOGLE_MAPS_API_BASE_URL + URL_FOR_IMAGE + mResults.get(index).getPhotos().get(0).getPhotoReference() + URL_FOR_IMAGE_KEY + BuildConfig.GOOGLE_BROWSER_API_KEY;
        Log.d(TAG, "setViewElementsForNearbyPlaces: " + imageUrl);
        Glide.with(this).load(imageUrl).into(this.fragmentDetailMainImage);
        // set Texts
        restaurantDetailTitle.setText(mResults.get(index).getName());
        restaurantDetailAddress.setText(mResults.get(index).getVicinity());
        // Set OpenNow Status (try & catch for null cases)
        try {
            if(mResults.get(index).getOpeningHours().getOpenNow()){
                restaurantDetailDescription.setText(getString(R.string.restaurant_detail_openNow));
            } else{
                restaurantDetailDescription.setText(getString(R.string.restaurant_detail_closed));
            }
        }
        catch(Exception e) {
            restaurantDetailDescription.setText(getString(R.string.restaurant_detail_openNow_notAvailable));
        }
        // set rating
        restaurantDetailLikes.setText(mResults.get(index).getRating().toString());
    }

    private void setViewElementsForAutoCompleteSheet(int index){
        // match the index with the Array index
        int i = index-100;
        try {
            // get the correct object from the list into TextViews
            restaurantDetailTitle.setText(mSearchAutosArray.get(i).getName());
            restaurantDetailAddress.setText(mSearchAutosArray.get(i).getAddress());
            // set rating
            restaurantDetailLikes.setText(mSearchAutosArray.get(i).getRating());
            // get image
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(mSearchAutosArray.get(i).getPhotoMeta().get(0))
                    .setMaxWidth(400).build();
            mPlacesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                fragmentDetailMainImage.setImageBitmap(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            });

            // Set Opening-Hours Status (try & catch for null cases)
            int day = dayToInteger();
            Log.d(TAG, "setViewElementsForAutoCompleteSheet: DAY TODAY  "+ day);
            String openingList = mSearchAutosArray.get(i).getOpeningHours().get(day);
            Log.d(TAG, "Opening times: " + openingList);
            if(openingList != null){
                restaurantDetailDescription.setText(openingList);
            } else{
                restaurantDetailDescription.setText(getString(R.string.restaurant_detail_openNow_notAvailable));
            }
        }
        catch(Exception e) {
            restaurantDetailDescription.setText(getString(R.string.restaurant_detail_not_available));
        }
    }

    private int dayToInteger (){
        // get the DAY_OF_WEEK and transform it to google's day counting (ex: Sunday 1 --> 6)
        int newDayInteger=-1;
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        switch (today){
            case Calendar.SUNDAY:
                newDayInteger = 6;
                break;
            case Calendar.MONDAY:
                newDayInteger = 0;
                break;
            case Calendar.TUESDAY:
                newDayInteger = 1;
                break;
            case Calendar.WEDNESDAY:
                newDayInteger = 2;
                break;
            case Calendar.THURSDAY:
                newDayInteger = 3;
                break;
            case Calendar.FRIDAY:
                newDayInteger = 4;
                break;
            case Calendar.SATURDAY:
                newDayInteger = 5;
                break;
        }
        return newDayInteger;
    }
}
