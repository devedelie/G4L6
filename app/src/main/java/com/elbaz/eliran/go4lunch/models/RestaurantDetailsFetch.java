package com.elbaz.eliran.go4lunch.models;

/**
 * Created by Eliran Elbaz on 09-Oct-19.
 */
public class RestaurantDetailsFetch {

    private String restaurantId;
    private String restaurantName;
    private int index;

    public RestaurantDetailsFetch(){}

    public RestaurantDetailsFetch(String restaurantId, String restaurantName, int index) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.index = index;
    }

    public String getRestaurantId() { return restaurantId; }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public int getIndex() { return index; }

    public void setIndex(int index) { this.index = index; }
}
