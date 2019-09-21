package com.elbaz.eliran.go4lunch.adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.fragments.ListViewFragment;
import com.elbaz.eliran.go4lunch.fragments.MapViewFragment;
import com.elbaz.eliran.go4lunch.fragments.WorkmatesFragment;

/**
 * Created by Eliran Elbaz on 21-Sep-19.
 */
public class PageAdapter extends FragmentStatePagerAdapter {
    private Context mContext;

    //Default Constructor
    public PageAdapter(Context context, FragmentManager mgr) {
        super(mgr, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
    }

    @Override
    public int getCount() {
        return(3);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new MapViewFragment();
            case 1:
                return new ListViewFragment();
            case 2:
                return new WorkmatesFragment();
            default:
                return null;
        }
    }

    @Override
    public String getPageTitle(int position) {
        switch (position){
            case 0:
                return mContext.getString(R.string.tab_name_1);
            case 1:
                return mContext.getString(R.string.tab_name_2);
            case 2:
                return mContext.getString(R.string.tab_name_3);
            default:
                return null;
        }
    }
}