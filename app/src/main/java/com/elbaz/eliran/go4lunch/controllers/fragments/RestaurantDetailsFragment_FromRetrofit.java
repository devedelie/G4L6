package com.elbaz.eliran.go4lunch.controllers.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
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
import com.elbaz.eliran.go4lunch.models.restaurantDetails.RestaurantDetails;
import com.elbaz.eliran.go4lunch.utils.PlacesStream;
import com.elbaz.eliran.go4lunch.views.RestaurantDetailAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.GOOGLE_MAPS_API_BASE_URL;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE;
import static com.elbaz.eliran.go4lunch.models.Constants.URL_FOR_IMAGE_KEY;
import static com.elbaz.eliran.go4lunch.utils.UtilsHelper.rating;

/**
 * Created by Eliran Elbaz on 29-Sep-19.
 */
public class RestaurantDetailsFragment_FromRetrofit extends BottomSheetDialogFragment implements RestaurantDetailAdapter.Listener {
    // FOR DESIGN
    @BindView(R.id.fragment_bottomsheet_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fragment_detail_image) ImageView fragmentDetailMainImage;
    @BindView(R.id.restaurant_details_star) ImageView starImage;
    @BindView(R.id.restaurantDetail_stars_1) ImageView starIcon1;
    @BindView(R.id.restaurantDetail_stars_2) ImageView starIcon2;
    @BindView(R.id.restaurantDetail_stars_3) ImageView starIcon3;
    @BindView(R.id.fragment_restaurant_detail_title) TextView restaurantDetailTitle;
    @BindView(R.id.fragment_restaurant_detail_address) TextView restaurantDetailAddress;
    @BindView(R.id.fragment_restaurant_detail_description) TextView restaurantDetailDescription;
    @BindView(R.id.detail_restaurant_likes) TextView restaurantDetailLikes;
    @BindView(R.id.addRestaurantFloatingActionButton) FloatingActionButton floatingActionButton;
    @BindView(R.id.empty_list_in_restaurant_detail) TextView emptyListText;
    // Bundle
    private static final String MARKER_RESTAURANT_ID = "MARKER_RESTAURANT_ID";
    private static final String MARKER_RESTAURANT_NAME = "MARKER_RESTAURANT_NAME";
    private static final String FIRESTORE_GOING_USERS_COLLECTION = "goingUsers";
    private String restaurantIDFromTag;
    private String restaurantNameFromTag;
    private Disposable mDisposable;
    private RestaurantDetails mRestaurantDetails;
    // Variables for Firestore
    private boolean mIsGoing = false; 
    private String mSavedRestaurantNameOnFB ="";
    private User modelCurrentUser;
    private String mCurrentSelectedRestaurantOnLoad;
    // RecyclerView Adapter
    private RestaurantDetailAdapter mRestaurantDetailAdapter;
    private List<String> likes = new ArrayList<>();
    private boolean mStarFlag = false;


    public static RestaurantDetailsFragment_FromRetrofit newInstance(String restaurantID, String restaurantName) {
        RestaurantDetailsFragment_FromRetrofit restaurantDetailsFragmentFromRetrofit;
        restaurantDetailsFragmentFromRetrofit = new RestaurantDetailsFragment_FromRetrofit();
        Bundle bundle = new Bundle();
        bundle.putString(MARKER_RESTAURANT_ID, restaurantID);
        bundle.putString(MARKER_RESTAURANT_NAME, restaurantName);
        restaurantDetailsFragmentFromRetrofit.setArguments(bundle);
        return restaurantDetailsFragmentFromRetrofit;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_details, container, false);
        ButterKnife.bind(this, view);
        // Get details from Bundle
        restaurantIDFromTag = getArguments().getString(MARKER_RESTAURANT_ID);
        restaurantNameFromTag = getArguments().getString(MARKER_RESTAURANT_NAME);
        this.executeHttpRequestForDetails();
        // Get user from Firestore
//        this.getCurrentUserFromFirestore();
        return view;
    }

    //-----------------
    // HTTP (RxJAVA)
    //-----------------

    // Execute the stream to fetch restaurant details
    private void executeHttpRequestForDetails(){
        // Execute the stream subscribing to Observable defined inside PlacesResults
        this.mDisposable = PlacesStream.streamFetchRestaurantDetailsByID(restaurantIDFromTag)
                .subscribeWith(new DisposableObserver<RestaurantDetails>(){
                    @Override
                    public void onNext(RestaurantDetails restaurantDetails) {
                        mRestaurantDetails = new RestaurantDetails();
                        mRestaurantDetails = restaurantDetails;
                        Log.d(TAG, "onNext: " + mRestaurantDetails.getResult().getName() + " " + mRestaurantDetails.getResult().getPlaceId());
                        // Call and fetch data from Firestore
                        getDataFromFireStore();
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onErrorHTTP: "+ e );
                    }
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    // This method will be called onDestroy to avoid any risk of memory leaks.
    private void disposeWhenDestroy(){
        if (this.mDisposable != null && !this.mDisposable.isDisposed()) this.mDisposable.dispose();
    }

    private void updateUI(){
        this.configureFloatingButton();
        this.setViewElements();
        this.configureRecyclerView();
    }

    private void configureFloatingButton(){
        // Compare the selected restaurant with the one from Firestore
        if(mSavedRestaurantNameOnFB.equals(mRestaurantDetails.getResult().getName()) && mIsGoing){
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
            String imageUrl = GOOGLE_MAPS_API_BASE_URL + URL_FOR_IMAGE + mRestaurantDetails.getResult().getPhotos().get(0).getPhotoReference() + URL_FOR_IMAGE_KEY + BuildConfig.GOOGLE_BROWSER_API_KEY;
            Log.d(TAG, "setViewElementsForNearbyPlaces: " + imageUrl);
            Glide.with(this).load(imageUrl).into(this.fragmentDetailMainImage);
            // set Texts
            restaurantDetailTitle.setText(mRestaurantDetails.getResult().getName());
            restaurantDetailAddress.setText(mRestaurantDetails.getResult().getVicinity());
            this.calculateStarRating(mRestaurantDetails);
            // Set OpenNow Status (try & catch for null cases)
            if(mRestaurantDetails.getResult().getOpeningHours().getOpenNow()){
                restaurantDetailDescription.setText(getString(R.string.restaurant_detail_openNow));
            } else{
                restaurantDetailDescription.setText(getString(R.string.restaurant_detail_closed));
            }
            // Check if user likes the current restaurant
            for(int i= 0 ; i<likes.size() ; i++){
                if(likes.get(i).equals(restaurantIDFromTag)){
                    mStarFlag = true;
                    starImage.setImageResource(R.drawable.ic_star_yellow_24dp);
                }
            }
        }
        catch(Exception e) {
            Log.d(TAG, "setViewElements: Error"+ e);
        }
    }

    private void calculateStarRating(RestaurantDetails restaurantDetails){
        try{
            double ratingValue = rating(restaurantDetails.getResult().getRating()); // round the value of rating
            if(ratingValue == 1){
                starIcon1.setVisibility(View.VISIBLE);
            }else if(ratingValue == 2){
                starIcon1.setVisibility(View.VISIBLE);
                starIcon2.setVisibility(View.VISIBLE);
            }else if(ratingValue == 3){
                starIcon1.setVisibility(View.VISIBLE);
                starIcon2.setVisibility(View.VISIBLE);
                starIcon3.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            Log.d(TAG, "calculateStarRating: "+ e);
        }
    }

    @OnClick(R.id.addRestaurantFloatingActionButton)
    public void addTodaysRestaurant (){
        Log.d(TAG, "addTodaysRestaurant: " + mSavedRestaurantNameOnFB + " " + mRestaurantDetails.getResult().getName() + " " + mIsGoing);
        // Change mIsGoing status & button color + icon
        if (mSavedRestaurantNameOnFB.equals(mRestaurantDetails.getResult().getName()) && mIsGoing){
            setFloatingActionButtonRed();
            mIsGoing = false;
        }else{
            setFloatingActionButtonGreen();
            mIsGoing = true;
            mSavedRestaurantNameOnFB = mRestaurantDetails.getResult().getName();
        }
        Log.d(TAG, "addTodaysRestaurant: is user going ? " + mIsGoing);
        updateUserOnFireStore();
    }

    private void updateUserOnFireStore(){
        // update Firestore DB with the current data
        if (this.getCurrentUser() != null){
            // set/remove restaurant name from user's document
            if(mIsGoing){
                UserHelper.updateTodaysRestaurant(this.getCurrentUser().getUid(), mRestaurantDetails.getResult().getName()).addOnFailureListener(this.onFailureListener());
                UserHelper.updateRestaurantID(this.getCurrentUser().getUid(), mRestaurantDetails.getResult().getPlaceId());
                UserHelper.updateTodaysRestaurantAddress(this.getCurrentUser().getUid(), mRestaurantDetails.getResult().getVicinity()).addOnFailureListener(this.onFailureListener());

            }else{
                UserHelper.updateTodaysRestaurant(this.getCurrentUser().getUid(), "");
                UserHelper.updateRestaurantID(this.getCurrentUser().getUid(), "");
                UserHelper.updateTodaysRestaurantAddress(this.getCurrentUser().getUid(),"");
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
                // Then, create a new "going-user" document to restaurant collection on Firestore
                GoingUserHelper.createUserForGoingList(this.mRestaurantDetails.getResult().getPlaceId(), mRestaurantDetails.getResult().getName(), modelCurrentUser).addOnFailureListener(this.onFailureListener());

            }else{
                // Delete document from going-user collection
                GoingUserHelper.deleteUserFromGoingList(mRestaurantDetails.getResult().getName(), modelCurrentUser).addOnFailureListener(this.onFailureListener());
            }
        }
    }

    @OnClick(R.id.restaurant_details_call_button)
    public void callToRestaurant(){
        if(mRestaurantDetails.getResult().getFormattedPhoneNumber()!=null && !mRestaurantDetails.getResult().getFormattedPhoneNumber().isEmpty()){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mRestaurantDetails.getResult().getFormattedPhoneNumber()));
            startActivity(intent);
        }else{
            Toast.makeText(getActivity(), R.string.restaurant_detail_call_no_number, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.restaurant_details_like_button)
    public void likeRestaurantBtn(){
        if (mStarFlag){  // User already liked the current restaurant (need to remove)
            likes.remove(restaurantIDFromTag);
            UserHelper.updateLikedRestaurants(getCurrentUser().getUid(), likes).addOnFailureListener(this.onFailureListener());
            starImage.setImageResource(R.drawable.ic_star_icon);
            mStarFlag=false;

        }else{      // User like the restaurant (need to add)
            likes.add(restaurantIDFromTag);
            UserHelper.updateLikedRestaurants(getCurrentUser().getUid(), likes).addOnFailureListener(this.onFailureListener());
            starImage.setImageResource(R.drawable.ic_star_yellow_24dp);
            mStarFlag=true;
        }
    }

    @OnClick(R.id.restaurant_details_website_button)
    public void goToWebsite(){
        if(mRestaurantDetails.getResult().getWebsite() != null && !mRestaurantDetails.getResult().getWebsite().isEmpty()){
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(mRestaurantDetails.getResult().getWebsite())));
        }else{
            Toast.makeText(getActivity(), R.string.restaurant_detail_call_no_website, Toast.LENGTH_LONG).show();
        }
    }

    private void configureRecyclerView() {
        //Configure Adapter & RecyclerView
        this.mRestaurantDetailAdapter = new RestaurantDetailAdapter(generateOptionsForAdapter(RestaurantHelper.getRestaurantCollection().document(mRestaurantDetails.getResult().getName()).collection(FIRESTORE_GOING_USERS_COLLECTION)), Glide.with(this), this, this.getCurrentUser().getUid());
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
    //  Get Current User's data from Firestore
    private void getDataFromFireStore(){
        //  Get additional data from Firestore (restaurant Name, ID & isGoing)
        UserHelper.getUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                User currentUser = documentSnapshot.toObject(User.class);
                modelCurrentUser = documentSnapshot.toObject(User.class);
                mSavedRestaurantNameOnFB = modelCurrentUser.getSelectedRestaurantName();
                mIsGoing = modelCurrentUser.getIsGoing();
                if(modelCurrentUser.getLikes() != null){
                    likes = modelCurrentUser.getLikes();
                }
                Log.d(TAG, "onSuccess Likes: " + likes);
                // keep the current restaurant name which saved on user's document, to help erasing it from isGoing collection if the user change restaurant
                mCurrentSelectedRestaurantOnLoad = modelCurrentUser.getSelectedRestaurantName();
                Log.d(TAG, "TEST onSuccess: "+ mSavedRestaurantNameOnFB + " " +mCurrentSelectedRestaurantOnLoad);
                // Update the UI while all data has been fetched (httpRequest & Firestore Data)
                updateUI();
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

}
