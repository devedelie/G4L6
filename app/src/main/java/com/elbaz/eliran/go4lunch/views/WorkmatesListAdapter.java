package com.elbaz.eliran.go4lunch.views;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestManager;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

/**
 * Created by Eliran Elbaz on 05-Oct-19.
 */
public class WorkmatesListAdapter extends FirestoreRecyclerAdapter <User, WorkmatesViewHolder> {

    public interface Listener {
        void onDataChanged();
    }

    //FOR DATA
    private final RequestManager glide;
    private final String idCurrentUser;
    private User modelCurrentUser;
    private int index;

    //FOR COMMUNICATION
    private Listener callback;

    public WorkmatesListAdapter(@NonNull FirestoreRecyclerOptions<User> options, RequestManager glide, Listener callback, String idCurrentUser) {
        super(options);
        this.glide = glide;
        this.idCurrentUser = idCurrentUser;
        this.callback = callback;
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmatesViewHolder workmatesViewHolder, int i, @NonNull User user) {
        // Get resources to enable text color modification
        Resources resources = workmatesViewHolder.itemView.getContext().getResources();
        // set the user
        modelCurrentUser = user;
        workmatesViewHolder.updateWorkmatesList(user, this.idCurrentUser, this.glide, resources);

    }

    @NonNull
    @Override
    public WorkmatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WorkmatesViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workmates_detail_item, parent, false));
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }

}
