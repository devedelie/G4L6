package com.elbaz.eliran.go4lunch.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        return view;
    }

    protected abstract int getFragmentLayout();

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    public FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }


//    //  Get Current User from Firestore
//    protected User getCurrentUserFromFirestore(){
//        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                modelCurrentUser = documentSnapshot.toObject(User.class);
//                Log.d(TAG, "onSuccess: getCurrentUserFromFirestore" + modelCurrentUser);
//            }
//        });
//        return modelCurrentUser;
//    }
}
