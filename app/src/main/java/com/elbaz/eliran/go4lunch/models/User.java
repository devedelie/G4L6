package com.elbaz.eliran.go4lunch.models;

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
    private Boolean isGoing;

    public User() { }

    public User(String uid, String username, String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.selectedRestaurantName = "";
        this.isGoing = false;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getUrlPicture() { return urlPicture; }
    public String getSelectedRestaurantName() { return selectedRestaurantName; }
    public Boolean getIsGoing() {return isGoing; }


    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(String urlPicture) { this.urlPicture = urlPicture; }
    public void setSelectedRestaurantName(String selectedRestaurantName) { this.selectedRestaurantName = selectedRestaurantName; }
    public void setIsGoing(Boolean isGoing) { this.isGoing = isGoing; }
}
