package com.elbaz.eliran.go4lunch.api.google;

import com.google.android.gms.maps.GoogleMap;

import io.reactivex.disposables.Disposable;

/**
 * Created by Eliran Elbaz on 26-Sep-19.
 */
public class GetNearbyPlaces {
    private Disposable mDisposable;
    private GoogleMap mGoogleMap;
    private String googlePlaceData, url;

    //-----------------
    // HTTP (RxJAVA)
    //-----------------

    // 1 - Execute the stream
//    private void executeHttpRequestWithRetrofit(){
//        // 1.2 - Execute the stream subscribing to Observable defined inside NYTStream
//        this.mDisposable = GoogleStreams.streamFetchRestaurants(getString(R.string.home))
//                .subscribeWith(new DisposableObserver<PlacesModel>(){
//
//                    @Override
//                    public void onNext(PlacesModel placesModel) {
//                        // 1.3 - Update UI with list of titles
//                        Log.e(TAG, "onNext" );
//                        updateUI(placesModel.getResult());
//                    }
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(TAG, "onError: "+ e );
//                    }
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "onComplete");
//                        //****************
////                        disposeWhenDestroy();
//                        //****************
//                    }
//                });
//    }

    // This method will be called onDestroy to avoid any risk of memory leaks.
    private void disposeWhenDestroy(){
        if (this.mDisposable != null && !this.mDisposable.isDisposed()) this.mDisposable.dispose();
    }
}
