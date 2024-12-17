package com.example.respondr;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserSession {
    private FirebaseAuth auth;

    public UserSession() {
        this.auth = FirebaseAuth.getInstance();
    }

    // Check if the user is already logged in
    public boolean isLoggedIn() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null;  // User is logged in if not null
    }

    // Get the current user's email
    public String getUserEmail() {
        FirebaseUser user = auth.getCurrentUser();
        return (user != null) ? user.getEmail() : null;
    }

    // Logout method
    public void logout() {
        auth.signOut();
    }
}
