package com.elbaz.eliran.go4lunch.controllers.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkmatesFragment extends BaseFragment {

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workmates, container, false);
    }

}
