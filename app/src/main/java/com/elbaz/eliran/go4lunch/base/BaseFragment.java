package com.elbaz.eliran.go4lunch.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;

/**
 * Created by Eliran Elbaz on 25-Sep-19.
 */
public abstract class BaseFragment extends Fragment {
    public BaseFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(this.getFragmentLayout(), container, false);
        ButterKnife.bind(this, view);

        this.updateData();
        return view;
    }

    protected abstract int getFragmentLayout();
    protected abstract void updateData();
}
