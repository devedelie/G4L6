package com.elbaz.eliran.go4lunch.controllers.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.base.BaseFragment;
import com.elbaz.eliran.go4lunch.models.User;
import com.elbaz.eliran.go4lunch.views.WorkmatesListAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkmatesFragment extends BaseFragment implements WorkmatesListAdapter.Listener {
    @BindView(R.id.workmateView_recyclerView) RecyclerView workmatesRecyclerView;
    @BindView(R.id.workmates_recycler_view_empty) TextView emptyListText;

    // FOR DATA
    // Declaring Adapter and data
    private WorkmatesListAdapter mWorkmatesListAdapter;
    @Nullable
    private User modelCurrentUser;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_workmates;
    }

    @Override
    protected void updateData() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        ButterKnife.bind(this, view);
        this.configureRecyclerView();
        // Inflate the layout for this fragment
        return view;
    }

    private void configureRecyclerView() {
        //Configure Adapter & RecyclerView
        this.mWorkmatesListAdapter = new WorkmatesListAdapter(generateOptionsForAdapter(UserHelper.getUsersCollection()), Glide.with(this), this, this.getCurrentUser().getUid());
        mWorkmatesListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                workmatesRecyclerView.smoothScrollToPosition(mWorkmatesListAdapter.getItemCount()); // Scroll to bottom on new workmate added to the list
            }
        });
        workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        workmatesRecyclerView.setAdapter(this.mWorkmatesListAdapter);
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
