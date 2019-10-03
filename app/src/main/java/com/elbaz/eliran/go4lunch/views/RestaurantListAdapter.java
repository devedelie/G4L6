package com.elbaz.eliran.go4lunch.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
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
    // Glide object
    private RequestManager glide;

    public RestaurantListAdapter(List<Result> results, Context context, RequestManager glide) {
        mResults = results;
        mContext = context;
        this.glide = glide;
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
        restaurantListViewHolder.updateRestaurantsList(this.mResults.get(i), this.glide);
    }

    @Override
    public int getItemCount() {return this.mResults.size();}
}
