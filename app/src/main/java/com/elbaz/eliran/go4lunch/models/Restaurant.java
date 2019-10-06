package com.elbaz.eliran.go4lunch.models;

/**
 * Created by Eliran Elbaz on 06-Oct-19.
 */
public class Restaurant {
    private String restaurantName;
    private String restaurantID;
    private String dateCreated;
    private User userGoing;
    private String urlImage;

    public Restaurant(String restaurantName, String restaurantType, User userGoing, String urlImage) {
        this.restaurantName = restaurantName;
        this.restaurantID = restaurantType;
        this.userGoing = userGoing;
        this.urlImage = urlImage;
    }

    public Restaurant(String restaurantID, String restaurantName, User userGoing) {
        this.restaurantID = restaurantID;
        this.restaurantName = restaurantName;
        this.userGoing = userGoing;
    }

    public String getRestaurantName() { return restaurantName; }
    public String getRestaurantID() { return restaurantID; }
    public String getDateCreated() { return dateCreated; }
    public User getUserGoing() { return userGoing; }
    public String getUrlImage() { return urlImage; }


    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public void setRestaurantID(String restaurantID) { this.restaurantID = restaurantID; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
    public void setUserGoing(User userGoing) { this.userGoing = userGoing; }
    public void setUrlImage(String urlImage) { this.urlImage = urlImage; }
}
