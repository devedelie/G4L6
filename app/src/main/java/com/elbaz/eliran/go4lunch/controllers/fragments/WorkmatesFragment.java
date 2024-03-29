package com.elbaz.eliran.go4lunch.controllers.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.base.BaseFragment;
import com.elbaz.eliran.go4lunch.models.User;
import com.elbaz.eliran.go4lunch.utils.ItemClickSupport;
import com.elbaz.eliran.go4lunch.views.WorkmatesListAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesFragment extends BaseFragment implements WorkmatesListAdapter.Listener {
    @BindView(R.id.workmateView_recyclerView) RecyclerView workmatesRecyclerView;
    @BindView(R.id.workmates_recycler_view_empty) TextView emptyListText;
    private WorkmatesListAdapter mWorkmatesListAdapter;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_workmates;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        ButterKnife.bind(this, view);
        this.configureRecyclerView();
        this.configureOnClickRecyclerView();
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_search_icon).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    private void configureRecyclerView() {
        //Configure Adapter & RecyclerView
        this.mWorkmatesListAdapter = new WorkmatesListAdapter(generateOptionsForAdapter(UserHelper.getUsersCollection()),  Glide.with(this), this, this.getCurrentUser().getUid());
        mWorkmatesListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                workmatesRecyclerView.smoothScrollToPosition(mWorkmatesListAdapter.getItemCount()); // Scroll to bottom on new workmate added to the list
            }
        });
        workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        workmatesRecyclerView.setAdapter(this.mWorkmatesListAdapter);
    }

    // -----------------
    // ACTION RecyclerView onClick
    // -----------------
    //  Configure item click on RecyclerView
    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(workmatesRecyclerView, R.layout.fragment_workmates)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        String restaurantID = mWorkmatesListAdapter.getItem(position).getRestaurantID();
                        String restaurantName = mWorkmatesListAdapter.getItem(position).getSelectedRestaurantName();
                        if(!restaurantID.isEmpty() && restaurantID != null){
                            RestaurantDetailsFragment_FromRetrofit.newInstance(restaurantID, restaurantName).show(getActivity().getSupportFragmentManager(), getTag());
                        }
                    }
                });
    }

    @Override
    public void onDataChanged() {
        //  Show TextView in case RecyclerView is empty
        emptyListText.setVisibility(this.mWorkmatesListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    //  Create options for RecyclerView from a Query
    private FirestoreRecyclerOptions<User> generateOptionsForAdapter(Query query){
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
    }

}
