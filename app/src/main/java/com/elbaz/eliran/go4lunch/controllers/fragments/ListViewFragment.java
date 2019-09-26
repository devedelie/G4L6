package com.elbaz.eliran.go4lunch.controllers.fragments;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.base.BaseFragment;
import com.elbaz.eliran.go4lunch.models.Constants;
import com.elbaz.eliran.go4lunch.utils.SnackbarAndVibrations;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListViewFragment extends BaseFragment {
    private SharedViewModel mSharedViewModel;


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
    }


    @Override
    protected int getFragmentLayout() { return R.layout.fragment_list_view; }

    @Override
    protected void updateData() {

    }



}
