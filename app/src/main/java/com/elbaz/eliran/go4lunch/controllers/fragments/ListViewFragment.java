package com.elbaz.eliran.go4lunch.controllers.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.base.BaseFragment;
import com.elbaz.eliran.go4lunch.models.Constants;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.utils.SnackbarAndVibrations;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
import com.elbaz.eliran.go4lunch.views.RestaurantListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListViewFragment extends BaseFragment {
    private SharedViewModel mSharedViewModel;
    private List<Result> mResults;
    private RestaurantListAdapter mRestaurantListAdapter;
    @BindView(R.id.listView_recyclerView) RecyclerView listViewRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        ButterKnife.bind(this, view); //Configure Butterknife
        this.configureRecyclerView();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set ViewModel Elements under onActivityCreated() to scope it to the lifeCycle of the Fragment
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        // Get currentPagerItem for Auto-Complete-SearchBar
        mSharedViewModel.getPagerCurrentItem().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == Constants.LIST_VIEW_FRAGMENT){
//                    autoCompleteSearchBar();
                    SnackbarAndVibrations.showSnakbarMessage(getView(), "ListViewFragment Search Action");
                }
            }
        });
        // get fetched Results
        mSharedViewModel.getResults().observe(getViewLifecycleOwner(), new Observer<List<Result>>() {
            @Override
            public void onChanged(List<Result> results) {
                Log.d(TAG, "ListView onChanged: ");
                updateUI(results);
            }
        });

    }


    @Override
    protected int getFragmentLayout() { return R.layout.fragment_list_view; }

    @Override
    protected void updateData() { }

    //-----------------
    // RecyclerView Config
    //-----------------
    protected void configureRecyclerView(){
        Log.d(TAG, "ListView configureRecyclerView: ");
        mResults = new ArrayList<>();
        mRestaurantListAdapter = new RestaurantListAdapter(this.mResults, getActivity().getApplicationContext(), Glide.with(this));
        listViewRecyclerView.setAdapter(this.mRestaurantListAdapter);
        listViewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    //-----------------
    // Update UI
    //-----------------
    // Update UI showing news titles
    private void updateUI(List<Result> results){
        // completely erase the previous list of results each time
        // in order to avoid duplicating it due to  .addAll()
        mResults.clear();
        mResults.addAll(results);
        mRestaurantListAdapter.notifyDataSetChanged();
        Log.d(TAG, "ListView updateUI: ");
    }


}
