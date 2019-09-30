package com.elbaz.eliran.go4lunch.controllers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elbaz.eliran.go4lunch.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Eliran Elbaz on 29-Sep-19.
 */
public class RestaurantBottomSheetFragment extends BottomSheetDialogFragment {
    // FOR DESIGN
    @BindView(R.id.fragment_bottomsheet_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fragment_detail_image) ImageView fragmentDetailMainImage;
    private static final String KEY_PROJECT_ID = "KEY_PROJECT_ID";

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.setViewElements();
    }

    private void setViewElements(){
        // set image
        Glide.with(this).load(MapViewFragment.bottomSheetMainImageURL).into(this.fragmentDetailMainImage);
    }
}
