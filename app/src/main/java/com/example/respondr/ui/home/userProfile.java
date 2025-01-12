package com.example.respondr.ui.home;

public class userProfile {
    private String name;
    private String mobile;
    private String email;
    private String profileImageUrl;

    public userProfile() {
        // Default constructor required for Firestore deserialization
    }

    public userProfile(String name, String mobile, String email, String profileImageUrl) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
