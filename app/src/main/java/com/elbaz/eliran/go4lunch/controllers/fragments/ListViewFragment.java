package com.elbaz.eliran.go4lunch.controllers.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.base.BaseFragment;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.utils.ItemClickSupport;
import com.elbaz.eliran.go4lunch.views.RestaurantListAdapter;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.controllers.activities.SplashScreen.mSharedViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListViewFragment extends BaseFragment {
    private List<Result> mResults = new ArrayList<>();
    private int sortValue;
    private RestaurantListAdapter mRestaurantListAdapter;
    @BindView(R.id.listView_recyclerView) RecyclerView listViewRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        ButterKnife.bind(this, view); //Configure Butterknife
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();
        setHasOptionsMenu(true); // Prepare the correct optionsMenu items
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // get fetched Results
        mSharedViewModel.getResultsList().observe(getViewLifecycleOwner(), new Observer<List<Result>>() {
            @Override
            public void onChanged(List<Result> results) {
                Log.d(TAG, "ListView onChanged: ");
                mResults.clear();
                mResults.addAll(results);
                updateUI(mResults);
            }
        });
    }

    // Prepare the correct optionsMenu items
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_sort_icon).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_list_view; }

    //-----------------
    // RecyclerView Config
    //-----------------
    protected void configureRecyclerView(){
        // Set the recyclerView to fixed size in order to increase performances
        listViewRecyclerView.setHasFixedSize(true);
        mResults = new ArrayList<>();
        mRestaurantListAdapter = new RestaurantListAdapter(this.mResults, getActivity().getApplicationContext());
        listViewRecyclerView.setAdapter(this.mRestaurantListAdapter);
        listViewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    // -----------------
    // ACTION RecyclerView onClick Configuration
    // -----------------
    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(listViewRecyclerView, R.layout.fragment_list_view)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        String restaurantID = mResults.get(position).getPlaceId();
                        String restaurantName = mResults.get(position).getName();
                        if(!restaurantID.isEmpty() && restaurantID != null){
                            RestaurantDetailsFragment_FromRetrofit.newInstance(restaurantID, restaurantName).show(getActivity().getSupportFragmentManager(), getTag());
                        }
                    }
                });
    }

    //-----------------
    // Update UI
    //-----------------
    // Update UI showing news titles
    private void updateUI(List<Result> results){
        // Notify changes
        mRestaurantListAdapter.notifyDataSetChanged();
    }

    // Update the UI with the result from Search-Autocomplete bar
    public void searchAction(Place place){
        String restaurantID = place.getId();
        String restaurantName = place.getName();
        if(!restaurantID.isEmpty() && restaurantID != null){
            RestaurantDetailsFragment_FromRetrofit.newInstance(restaurantID, restaurantName).show(getActivity().getSupportFragmentManager(), getTag());
        }
    }

    //-----------------
    // Sorting results
    //-----------------
    public void sortResults(String sortType){
        // sort the data
        if(mResults.size() > 0){
            Collections.sort(mResults, new Comparator<Result>() {
                @Override
                public int compare(Result o1, Result o2) {
                    switch (sortType){
                        case "Default":
                            break;
                        case "Distance":
                            sortValue = o1.getDistance().compareTo(o2.getDistance());
                            break;
                        case "Rating":
                            sortValue = o2.getRating().compareTo(o1.getRating());
                            break;
                        case "Workmates":
                            sortValue = o2.getWorkmates().compareTo(o1.getWorkmates());
                            break;
                    }
                    return sortValue;
                }
            });
        }
        mRestaurantListAdapter = new RestaurantListAdapter(this.mResults, getActivity().getApplicationContext());
        listViewRecyclerView.setAdapter(mRestaurantListAdapter);
    }

}
