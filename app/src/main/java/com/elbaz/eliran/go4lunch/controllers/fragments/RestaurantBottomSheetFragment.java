package com.elbaz.eliran.go4lunch.controllers.fragments;

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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
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
        // get the correct object from the list
        restaurantDetailTitle.setText(mSearchAutosArray.get(i).getName());
        restaurantDetailAddress.setText(mResults.get(i).getVicinity());
        // Set OpenNow Status (try & catch for null cases)
        try {
            if(mResults.get(i).getOpeningHours().getOpenNow()){
                restaurantDetailDescription.setText(getString(R.string.restaurant_detail_openNow));
            } else{
                restaurantDetailDescription.setText(getString(R.string.restaurant_detail_closed));
            }
        }
        catch(Exception e) {
            restaurantDetailDescription.setText(getString(R.string.restaurant_detail_openNow_notAvailable));
        }
        // set rating
        restaurantDetailLikes.setText(mResults.get(i).getRating().toString());
    }
}
