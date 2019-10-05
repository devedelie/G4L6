package com.elbaz.eliran.go4lunch.views;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;

/**
 * Created by Eliran Elbaz on 05-Oct-19.
 */
public class WorkmatesViewHolder extends RecyclerView.ViewHolder {
    // Workmates ListView items
    @BindView(R.id.workmate_detail_recyclerView_workmateImage) ImageView workmateImage;
    @BindView(R.id.workmate_detail_recyclerView_text_view) TextView workmateText;

    public WorkmatesViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWorkmatesList (User user, String currentUserId, RequestManager glide){
        try{
            //set images
            glide.load(user.getUsername()).apply(RequestOptions.centerCropTransform()).into(workmateImage);
            // set texts
            workmateText.setText(user.getUsername());
        }catch (Exception e){
            Log.d(TAG, "updateWorkmatesList: Error " + e);
        }
    }
}
