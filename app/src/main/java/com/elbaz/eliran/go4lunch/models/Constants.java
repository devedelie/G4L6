package com.elbaz.eliran.go4lunch.models;

/**
 * Created by Eliran Elbaz on 20-Sep-19.
 */
public class Constants {
    public static final Integer ERROR_DIALOG_REQUEST = 9001;
    public static final Integer PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final String GOOGLE_MAPS_API_BASE_URL = "https://maps.googleapis.com/";
    public static final String URL_FOR_IMAGE = "maps/api/place/photo?maxwidth=400&photoreference=";
    public static final String URL_FOR_IMAGE_KEY = "&key=";
    public static final String NEARBY_TYPE="restaurant";
    public static final int NEARBY_RADIUS= 700;
    public static final String SEARCH_FIELDS = "place_id,photo,name,vicinity,rating,formatted_phone_number,website,opening_hours,review";
    public static final String FIREBASE_DATA_MESSAGE_KEY = "BackgroundCall";
}
