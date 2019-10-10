package com.elbaz.eliran.go4lunch.controllers.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elbaz.eliran.go4lunch.BuildConfig;
import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.GoingUserHelper;
import com.elbaz.eliran.go4lunch.api.RestaurantHelper;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.models.Restaurant;
import com.elbaz.eliran.go4lunch.models.User;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
import com.elbaz.eliran.go4lunch.views.RestaurantDetailAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.GOOGLE_MAPS_API_BASE_URL;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE_KEY;

/**
 * Created by Eliran Elbaz on 29-Sep-19.
 */
public class RestaurantDetailsFragment_FromViewModel extends BottomSheetDialogFragment implements RestaurantDetailAdapter.Listener {
    // FOR DESIGN
    @BindView(R.id.fragment_bottomsheet_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fragment_detail_image) ImageView fragmentDetailMainImage;
    @BindView(R.id.fragment_restaurant_detail_title) TextView restaurantDetailTitle;
    @BindView(R.id.fragment_restaurant_detail_address) TextView restaurantDetailAddress;
    @BindView(R.id.fragment_restaurant_detail_description) TextView restaurantDetailDescription;
    @BindView(R.id.detail_restaurant_likes) TextView restaurantDetailLikes;
    @BindView(R.id.addRestaurantFloatingActionButton) FloatingActionButton floatingActionButton;
    @BindView(R.id.empty_list_in_restaurant_detail) TextView emptyListText;
    // Bundle
    private static final String MARKER_RESTAURANT_ID = "MARKER_RESTAURANT_ID";
    private static final String MARKER_RESTAURANT_NAME = "MARKER_RESTAURANT_NAME";
    private static final String MARKER_RESTAURANT_INDEX = "MARKER_RESTAURANT_NAME";
    private static final String FIRESTORE_GOING_USERS_COLLECTION = "goingUsers";
    private String restaurantIDFromTag;
    private String restaurantNameFromTag;
    private PlacesClient mPlacesClient;
    private int mIndex;
    // Variables for Firestore
    private boolean mIsGoing = false;
    private String mSavedRestaurantNameOnFB ="";
    private User modelCurrentUser;
    private String mCurrentSelectedRestaurantOnLoad;
    private RestaurantDetailAdapter mRestaurantDetailAdapter;
    // Data
    private List<Result> mResults;


    public static RestaurantDetailsFragment_FromViewModel newInstance(String restaurantID, String restaurantName, int index) {
        RestaurantDetailsFragment_FromViewModel restaurantDetailsFragment_fromViewModel;
        restaurantDetailsFragment_fromViewModel = new RestaurantDetailsFragment_FromViewModel();
        Bundle bundle = new Bundle();
        bundle.putString(MARKER_RESTAURANT_ID, restaurantID);
        bundle.putString(MARKER_RESTAURANT_NAME, restaurantName);
        bundle.putInt(MARKER_RESTAURANT_INDEX, index);
        restaurantDetailsFragment_fromViewModel.setArguments(bundle);
        return restaurantDetailsFragment_fromViewModel ;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_details
                , container, false);
        ButterKnife.bind(this, view);
        // Get details from Bundle
        restaurantIDFromTag = getArguments().getString(MARKER_RESTAURANT_ID);
        restaurantNameFromTag = getArguments().getString(MARKER_RESTAURANT_NAME);
        mIndex = getArguments().getInt(MARKER_RESTAURANT_INDEX);
//        mPlacesClient = Places.createClient(getActivity());
        this.getCurrentUserFromFirestore();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set ViewModel Elements under onActivityCreated() to scope it to the lifeCycle of the Fragment
        SharedViewModel mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        // Observe Search ArrayList
        mSharedViewModel.getResultsList().observe(getViewLifecycleOwner(), new Observer <List<Result>>() {
            @Override
            public void onChanged(List<Result> result) {
                // update the array with new added objects
                mResults = new ArrayList<>();
                mResults.clear();
                mResults.addAll(result);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getDataFromFireStore();
        setViewElements();
        getCurrentUserFromFirestore();
    }

    private void getDataFromFireStore(){
        //  Get additional data from Firestore (restaurant name & isGoing)
        UserHelper.getUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);
                mSavedRestaurantNameOnFB = currentUser.getSelectedRestaurantName();
                mIsGoing = currentUser.getIsGoing();
                // keep the current restaurant name which saved on user's document, to help erasing it from isGoing collection if the user change restaurant
                mCurrentSelectedRestaurantOnLoad = currentUser.getSelectedRestaurantName();
                Log.d(TAG, "onSuccess: "+ mSavedRestaurantNameOnFB + " " +mCurrentSelectedRestaurantOnLoad);
                // Configure FloatingButton from inside onSuccess, to avoid empty variable case
                updateUI();
            }
        });
    }

    private void updateUI(){
        this.configureFloatingButton();
        this.setViewElements();
        this.configureRecyclerView();
    }

    private void configureFloatingButton(){
        // Compare the selected restaurant with the one from Firestore
        if(mSavedRestaurantNameOnFB.equals(mResults.get(mIndex).getName()) && mIsGoing){
            setFloatingActionButtonGreen();
        }else{
            setFloatingActionButtonRed();
        }
    }

    private void setFloatingActionButtonGreen(){
        floatingActionButton.setImageResource(R.drawable.ic_check);
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.floating_btn_green)));
    }

    private void setFloatingActionButtonRed(){
        floatingActionButton.setImageResource(R.drawable.ic_add_icon);
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gmail_btn_color)));
    }


    private void setViewElements(){
        try {
            // Set Image reference string and set image with Glide
            String imageUrl = GOOGLE_MAPS_API_BASE_URL + URL_FOR_IMAGE + mResults.get(mIndex).getPhotos().get(0).getPhotoReference() + URL_FOR_IMAGE_KEY + BuildConfig.GOOGLE_BROWSER_API_KEY;
            Log.d(TAG, "setViewElementsForNearbyPlaces: " + imageUrl);
            Glide.with(this).load(imageUrl).into(this.fragmentDetailMainImage);
            // set Texts
            restaurantDetailTitle.setText(mResults.get(mIndex).getName());
            restaurantDetailAddress.setText(mResults.get(mIndex).getVicinity());
            // Set OpenNow Status (try & catch for null cases)
            if(mResults.get(mIndex).getOpeningHours().getOpenNow()){
                restaurantDetailDescription.setText(getString(R.string.restaurant_detail_openNow));
            } else{
                restaurantDetailDescription.setText(getString(R.string.restaurant_detail_closed));
            }
            // set rating
            restaurantDetailLikes.setText(mResults.get(mIndex).getRating().toString());

        }
        catch(Exception e) {
            restaurantDetailDescription.setText(getString(R.string.restaurant_detail_not_available));
        }
    }

    @OnClick(R.id.addRestaurantFloatingActionButton)
    public void addTodaysRestaurant (){
        Log.d(TAG, "addTodaysRestaurant: " + mSavedRestaurantNameOnFB + " " + mResults.get(mIndex).getName() + " " + mIsGoing);
        // Change mIsGoing status & button color + icon
        if (mSavedRestaurantNameOnFB.equals(mResults.get(mIndex).getName()) && mIsGoing){
            setFloatingActionButtonRed();
            mIsGoing = false;
        }else{
            setFloatingActionButtonGreen();
            mIsGoing = true;
            mSavedRestaurantNameOnFB = mResults.get(mIndex).getName();
        }
        Log.d(TAG, "addTodaysRestaurant: is user going ? " + mIsGoing);
        updateUserOnFireStore();
    }


    private void updateUserOnFireStore(){
        // update Firestore DB with the current data
        if (this.getCurrentUser() != null){
            // set/remove restaurant name from user's document
            if(mIsGoing){
                UserHelper.updateTodaysRestaurant(this.getCurrentUser().getUid(), mResults.get(mIndex).getName()).addOnFailureListener(this.onFailureListener());
                UserHelper.updateRestaurantID(this.getCurrentUser().getUid(), mResults.get(mIndex).getPlaceId());
            }else{
                UserHelper.updateTodaysRestaurant(this.getCurrentUser().getUid(), "");
                UserHelper.updateRestaurantID(this.getCurrentUser().getUid(), "");
            }
            // set the current isGoing status in user's document
            UserHelper.updateIsGoing(this.getCurrentUser().getUid(), mIsGoing);
            // create/update 'going-user' document inside Restaurant collection (Restaurants --> {restaurant name} --> goingUsers --> user#)
            updateRestaurantCollection();
        }
    }

    private void updateRestaurantCollection(){
        if ( modelCurrentUser != null){
            if(mIsGoing){
                if(mCurrentSelectedRestaurantOnLoad != null && !mCurrentSelectedRestaurantOnLoad.isEmpty()){
                    // If Exists, delete the old value of selected restaurant
                    GoingUserHelper.deleteUserFromPreviousGoingList(modelCurrentUser, mCurrentSelectedRestaurantOnLoad);
                }
                // Create a new "going-user" document to restaurant collection on Firestore
                GoingUserHelper.createUserForGoingList(this.mResults.get(mIndex).getId(), mResults.get(mIndex).getName(), modelCurrentUser).addOnFailureListener(this.onFailureListener());

            }else{
                // Delete document from going-user collection
                GoingUserHelper.deleteUserFromGoingList(mResults.get(mIndex).getName(), modelCurrentUser).addOnFailureListener(this.onFailureListener());
            }
        }
    }

    private void configureRecyclerView() {
        //Configure Adapter & RecyclerView
        this.mRestaurantDetailAdapter = new RestaurantDetailAdapter(generateOptionsForAdapter(RestaurantHelper.getRestaurantCollection().document(mResults.get(mIndex).getName()).collection(FIRESTORE_GOING_USERS_COLLECTION)), Glide.with(this), this, this.getCurrentUser().getUid());
        mRestaurantDetailAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(mRestaurantDetailAdapter.getItemCount()); // Scroll to bottom on new workmate added to the list
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(this.mRestaurantDetailAdapter);
    }

    //  Create options for RecyclerView from a Query
    private FirestoreRecyclerOptions<Restaurant> generateOptionsForAdapter(CollectionReference query){
        return new FirestoreRecyclerOptions.Builder<Restaurant>()
                .setQuery(query,Restaurant.class)
                .setLifecycleOwner(this)
                .build();
    }

    // --------------------
    // CALLBACK
    // --------------------

    @Override
    public void onDataChanged() {
        //  Show TextView in case RecyclerView is empty
        emptyListText.setVisibility(this.mRestaurantDetailAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // --------------------
    // UTILS
    // --------------------
    @Nullable
    private FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    // --------------------
    // REST REQUESTS
    // --------------------
    //  Get Current User from Firestore
    private void getCurrentUserFromFirestore(){
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                modelCurrentUser = documentSnapshot.toObject(User.class);
                Log.d(TAG, "onSuccess: getCurrentUserFromFirestore" + modelCurrentUser);
            }
        });
    }


    // --------------------
    // ERROR HANDLER
    // --------------------

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
                Log.d(TAG, "onFailure: " +e);
            }
        };
    }

//    private int dayToInteger (){
//        // get the DAY_OF_WEEK and transform it to google's day counting (ex: Sunday 1 --> 6)
//        int newDayInteger=-1;
//        Calendar calendar = Calendar.getInstance();
//        int today = calendar.get(Calendar.DAY_OF_WEEK);
//        switch (today){
//            case Calendar.SUNDAY:
//                newDayInteger = 6;
//                break;
//            case Calendar.MONDAY:
//                newDayInteger = 0;
//                break;
//            case Calendar.TUESDAY:
//                newDayInteger = 1;
//                break;
//            case Calendar.WEDNESDAY:
//                newDayInteger = 2;
//                break;
//            case Calendar.THURSDAY:
//                newDayInteger = 3;
//                break;
//            case Calendar.FRIDAY:
//                newDayInteger = 4;
//                break;
//            case Calendar.SATURDAY:
//                newDayInteger = 5;
//                break;
//        }
//        return newDayInteger;
//    }

}
