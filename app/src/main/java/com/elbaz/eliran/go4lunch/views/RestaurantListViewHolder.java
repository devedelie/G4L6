package com.elbaz.eliran.go4lunch.views;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;

import butterknife.ButterKnife;

/**
 * Created by Eliran Elbaz on 02-Oct-19.
 */
public class RestaurantListViewHolder extends RecyclerView.ViewHolder {

    public RestaurantListViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateRestaurantsList (Result result, RequestManager glide){
        
    }


}
