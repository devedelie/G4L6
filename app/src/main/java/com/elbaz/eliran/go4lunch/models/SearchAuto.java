package com.elbaz.eliran.go4lunch.models;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.PhotoMetadata;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by Eliran Elbaz on 01-Oct-19.
 */
public class SearchAuto {

    static int objectId;
    private String id;
    private String name;
    private String address;
    private String phone;
    private List<String> openingHours;
    @Nullable private Uri websiteUri;
    private List<PhotoMetadata> photoMeta;
    private Integer pricing;
    private double rating;
    private LatLng locationLatLng;

    public SearchAuto (){}

    public SearchAuto(int objectId, String id, String name, String address, String phone, List<String> openingHours, Uri websiteUri, List<PhotoMetadata> photoMeta, double rating, LatLng locationLatLng) {
        this.objectId = objectId;
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.openingHours = openingHours;
        this.websiteUri = websiteUri;
        this.photoMeta = photoMeta;
        this.rating = rating;
        this.locationLatLng = locationLatLng;
    }

    public static int getObjectId() {
        return objectId;
    }

    public static void setObjectId(int objectId) {
        SearchAuto.objectId = objectId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(List<String> openingHours) {
        this.openingHours = openingHours;
    }

    public Uri getWebsiteUri() {
        return websiteUri;
    }

    public void setWebsiteUri(Uri websiteUri) {
        this.websiteUri = websiteUri;
    }

    public List<PhotoMetadata> getPhotoMeta() {
        return photoMeta;
    }

    public void setPhotoMeta(List<PhotoMetadata> photoMeta) {
        this.photoMeta = photoMeta;
    }

    public Integer getPricing() {
        return pricing;
    }

    public void setPricing(Integer pricing) {
        this.pricing = pricing;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public LatLng getLocationLatLng() {
        return locationLatLng;
    }

    public void setLocationLatLng(LatLng locationLatLng) {
        this.locationLatLng = locationLatLng;
    }
}


