package com.elbaz.eliran.go4lunch.models;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by Eliran Elbaz on 19-Sep-19.
 */
public class User {

    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private String selectedRestaurantName;
    private String selectedRestaurantAddress;
    private Boolean isGoing;
    private String restaurantID;
    private List<String> likes;

    public User() { }

    public User(String uid, String username, String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.selectedRestaurantName = "";
        this.selectedRestaurantAddress = "";
        this.isGoing = false;
        this.restaurantID = "";
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getUrlPicture() { return urlPicture; }
    public String getSelectedRestaurantName() { return selectedRestaurantName; }
    public Boolean getIsGoing() {return isGoing; }
    public String getRestaurantID() { return restaurantID; }
    public List<String> getLikes(){return likes;}
    public String getSelectedRestaurantAddress() { return selectedRestaurantAddress; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(String urlPicture) { this.urlPicture = urlPicture; }
    public void setSelectedRestaurantName(String selectedRestaurantName) { this.selectedRestaurantName = selectedRestaurantName; }
    public void setIsGoing(Boolean isGoing) { this.isGoing = isGoing; }
    public void setRestaurantID(String restaurantID) { this.restaurantID = restaurantID; }
    public void setLikes(List<String> likes){this.likes = likes;}
    public void setSelectedRestaurantAddress(String selectedRestaurantAddress) { this.selectedRestaurantAddress = selectedRestaurantAddress; }

}
