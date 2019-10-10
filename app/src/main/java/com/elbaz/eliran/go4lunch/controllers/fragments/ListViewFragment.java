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
import com.elbaz.eliran.go4lunch.utils.ItemClickSupport;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
import com.elbaz.eliran.go4lunch.views.RestaurantListAdapter;
import com.google.android.libraries.places.api.model.Place;

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
    private List<Result> mResultsSorted;
    private RestaurantListAdapter mRestaurantListAdapter;
    @BindView(R.id.listView_recyclerView) RecyclerView listViewRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        ButterKnife.bind(this, view); //Configure Butterknife
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();

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
                    updateSearchAutoComplete(); // update the Map-UI with the result
                }
            }
        });
        // get fetched Results
        mSharedViewModel.getResultsList().observe(getViewLifecycleOwner(), new Observer<List<Result>>() {
            @Override
            public void onChanged(List<Result> results) {
                Log.d(TAG, "ListView onChanged: ");
                updateUI(results);
            }
        });

    }

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_list_view; }

    //-----------------
    // RecyclerView Config
    //-----------------
    protected void configureRecyclerView(){
        Log.d(TAG, "ListView configureRecyclerView: ");
        // Set the recyclerView to fixed size in order to increase performances
        listViewRecyclerView.setHasFixedSize(true);
        mResults = new ArrayList<>();
        mRestaurantListAdapter = new RestaurantListAdapter(this.mResults, getActivity().getApplicationContext(), Glide.with(this));
        listViewRecyclerView.setAdapter(this.mRestaurantListAdapter);
        listViewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    // -----------------
    // ACTION RecyclerView onClick
    // -----------------
    //  Configure item click on RecyclerView
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
        // completely erase the previous list of results each time
        // in order to avoid duplicating it due to  .addAll()

//        // sort the data
//        if(results.size() > 0){
//            Collections.sort(results, new Comparator<Result>() {
//                @Override
//                public int compare(Result o1, Result o2) {
//                    return o2.getRating().compareTo(o1.getRating());
//                }
//            });
//        }
        // Notify changes
        mResults.clear();
        mResults.addAll(results);
        mRestaurantListAdapter.notifyDataSetChanged();
        Log.d(TAG, "ListView updateUI: ");
    }

    // Update the UI with the result from Search-Autocomplete bar
    public void updateSearchAutoComplete(){
        // Get place Object for Auto-Complete-SearchBar
        mSharedViewModel.getSearchObject().observe(getViewLifecycleOwner(), new Observer<Place>() {
            @Override
            public void onChanged(Place place) {
                Log.d(TAG, "TEST onChanged: place value changed");
                String restaurantID = place.getId();
                String restaurantName = place.getName();
                if(!restaurantID.isEmpty() && restaurantID != null){
                    RestaurantDetailsFragment_FromRetrofit.newInstance(restaurantID, restaurantName).show(getActivity().getSupportFragmentManager(), getTag());
                }
            }
        });

    }


}
