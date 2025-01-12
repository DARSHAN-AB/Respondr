package com.example.respondr.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.respondr.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class ProfileEditFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image selection
    private ImageView profileImageView;
    private EditText nameEditText, mobileEditText, emailEditText;
    private Button saveButton;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private Uri profileImageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bind UI elements
        nameEditText = view.findViewById(R.id.et_profile_name);
        mobileEditText = view.findViewById(R.id.et_mobile_number);
        emailEditText = view.findViewById(R.id.et_profile_email);
        saveButton = view.findViewById(R.id.btn_save_profile);
        profileImageView = view.findViewById(R.id.iv_profile_image);

        // Prefill the fields with current user data
        nameEditText.setText(auth.getCurrentUser().getDisplayName());
        emailEditText.setText(auth.getCurrentUser().getEmail());
        // Set click listener for the profile image
        profileImageView.setOnClickListener(v -> openGallery());
        saveButton.setOnClickListener(v -> {
            String updatedName = nameEditText.getText().toString().trim();
            String updatedEmail = emailEditText.getText().toString().trim();
            String updatedPhone = mobileEditText.getText().toString().trim();

            // Update the fields to null if they are empty
            if (TextUtils.isEmpty(updatedName)) updatedName = null;
            if (TextUtils.isEmpty(updatedEmail)) updatedEmail = null;
            if (TextUtils.isEmpty(updatedPhone)) updatedPhone = null;

            // Update Firestore
            String userId = auth.getCurrentUser().getUid();
            firestore.collection("users").document(userId).update(
                    "name", updatedName,
                    "email", updatedEmail,
                    "phone", updatedPhone
            ).addOnSuccessListener(aVoid -> {
                // After saving data, navigate back to ViewFragment
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.ProfileVeiwFragment);
            });
        });

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            profileImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), profileImageUri);
                profileImageView.setImageBitmap(bitmap); // Display the selected image in ImageView
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfileData() {
        String name = nameEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(mobile)) {
            mobileEditText.setError("Mobile number is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        // Get user ID
        String userId = auth.getCurrentUser().getUid();

        // Upload profile image to Firebase Storage
        if (profileImageUri != null) {
            StorageReference profileImageRef = storage.getReference("profile_images/" + userId + ".jpg");

            profileImageRef.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImageUrl = uri.toString();
                            // Create a user profile object with all the details
                            UserProfile userProfile = new UserProfile(name, mobile, email, profileImageUrl);

                            // Save the profile data in Firestore
                            firestore.collection("users").document(userId)
                                    .set(userProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                        // Optionally, navigate back to ProfileViewFragment or another fragment
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to update profile. Try again.", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to upload image. Try again.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If no image is selected, still save profile data without image
            UserProfile userProfile = new UserProfile(name, mobile, email, null);
            firestore.collection("users").document(userId)
                    .set(userProfile)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update profile. Try again.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // UserProfile class to hold the user's profile details
    public static class UserProfile {
        private String name;
        private String mobile;
        private String email;
        private String profileImageUrl;

        public UserProfile(String name, String mobile, String email, String profileImageUrl) {
            this.name = name;
            this.mobile = mobile;
            this.email = email;
            this.profileImageUrl = profileImageUrl;
        }

        public String getName() {
            return name;
        }

        public String getMobile() {
            return mobile;
        }

        public String getEmail() {
            return email;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }
    }
}
