package com.elbaz.eliran.go4lunch.controllers.fragments;

import android.os.Bundle;
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
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Eliran Elbaz on 29-Sep-19.
 */
public class RestaurantBottomSheetFragment extends BottomSheetDialogFragment {
    // FOR DESIGN
    @BindView(R.id.fragment_bottomsheet_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fragment_detail_image) ImageView fragmentDetailMainImage;
    @BindView(R.id.fragment_restaurant_detail_title) TextView restaurantDetailTitle;
    @BindView(R.id.fragment_restaurant_detail_description) TextView restaurantDetailDescription;
    @BindView(R.id.detail_restaurant_likes) TextView restaurantDetailLikes;
    private SharedViewModel mSharedViewModel;
    private List<Result> mResults;

    public static RestaurantBottomSheetFragment newInstance(Integer projectId) {
        RestaurantBottomSheetFragment restaurantBottomSheetFragment;
        restaurantBottomSheetFragment = new RestaurantBottomSheetFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt(KEY_PROJECT_ID, projectId);
//        restaurantBottomSheetFragment.setArguments(bundle);
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
        // get fetched Results
        mSharedViewModel.getResults().observe(getViewLifecycleOwner(), new Observer<List<Result>>() {
            @Override
            public void onChanged(List<Result> results) {
                // set results
                mResults = new ArrayList<>();
                mResults.clear();
                mResults.addAll(results);
                setViewElements();
            }
        });
    }

    private void setViewElements(){
        // set image
        Glide.with(this).load(MapViewFragment.bottomSheetMainImageURL).into(this.fragmentDetailMainImage);
        // set Texts
        restaurantDetailTitle.setText(mResults.get(0).getName());
        if(mResults.get(0).getOpeningHours().getOpenNow()){
            restaurantDetailDescription.setText(getString(R.string.restaurant_detail_openNow));
        } else{
            restaurantDetailDescription.setText(getString(R.string.restaurant_detail_closed));
        }
        // set rating
        restaurantDetailLikes.setText(mResults.get(0).getRating().toString());
    }
}
