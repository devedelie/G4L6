package com.elbaz.eliran.go4lunch.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Eliran Elbaz on 02-Oct-19.
 */
public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListViewHolder> {

    // DATA
    private List<Result> mResults;
    Context mContext;

    public RestaurantListAdapter(List<Result> results, Context context) {
        mResults = results;
        mContext = context;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RestaurantListViewHolder onCreateViewHolder (@NonNull ViewGroup viewGroup, int i){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.restaurant_detail_item, viewGroup, false);

        return new RestaurantListViewHolder(view);
    }

    @Override
    public void onBindViewHolder (@NonNull RestaurantListViewHolder restaurantListViewHolder, int i){
        Log.d(TAG, "ListView onBindViewHolder: ");
        restaurantListViewHolder.updateRestaurantsList(this.mResults.get(i), Glide.with(mContext));
    }

    @Override
    public int getItemCount() {return this.mResults.size();}

    //setHasStableIds(true) & the methods below are making an optimization while providing data to ViewHolder, to keep id as unique and unchangeable.
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
