package com.example.respondr.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.respondr.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;

public class ProfileVeiwFragment extends Fragment {

    private TextView nameTextView, mobileTextView, emailTextView, adressTextView;
    private ImageView profileImageView;
    private String userId;
    private Button editProfileButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_veiw, container, false);

        // Initialize views
        nameTextView = view.findViewById(R.id.et_veiwprofile_name);
        mobileTextView = view.findViewById(R.id.et_veiwmobile_number);
        emailTextView = view.findViewById(R.id.et_veiwprofile_email);
        profileImageView = view.findViewById(R.id.iv_veiwprofile_image);
        editProfileButton = view.findViewById(R.id.btn_edit_profile);
        adressTextView = view.findViewById(R.id.et_veiwaddress);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        // Fetch and display user data from Firestore
        loadUserProfileData();


        // Navigate to Edit Fragment when the button is clicked
        editProfileButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.profileEditFragment);
        });

        return view;
    }

    // This method will load the user data from Firestore
    // This method will load the user data from Firestore
    private void loadUserProfileData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String mobile = documentSnapshot.getString("mobile");
                String address = documentSnapshot.getString("address"); // Fetch address
                String photoUrl = documentSnapshot.getString("photoUrl");

                nameTextView.setText(name != null ? name : "Add your name");
                emailTextView.setText(email != null ? email : "Add your email");
                mobileTextView.setText(mobile != null ? mobile : "Add your mobile");
                adressTextView.setText(address != null ? address : "Add your address"); // Set address
                if (photoUrl != null) {
                    loadImageFromUrl(photoUrl);
                } else {
                    profileImageView.setImageResource(R.drawable.baseline_person_24); // Default image
                }
            }
        });
    }

    // Method to load image from URL
    private void loadImageFromUrl(String url) {
        new Thread(() -> {
            try {
                // Create a URL object
                java.net.URL imageUrl = new java.net.URL(url);
                // Open a connection to the URL
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                // Get the input stream
                InputStream input = connection.getInputStream();
                // Decode the input stream into a Bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                // Update the ImageView on the UI thread
                getActivity().runOnUiThread(() -> profileImageView.setImageBitmap(bitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

