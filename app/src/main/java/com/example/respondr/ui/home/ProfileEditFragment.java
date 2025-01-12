package com.example.respondr.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProfileEditFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image selection
    private ImageView profileImageView;
    private EditText nameEditText, mobileEditText, emailEditText, adressEditText;
    private Button saveButton;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private FirebaseUser currentUser ;
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
        adressEditText = view.findViewById(R.id.et_address);

        // Prefill the fields with current user data
        nameEditText.setText(auth.getCurrentUser().getDisplayName());
        emailEditText.setText(auth.getCurrentUser().getEmail());
        firestore = FirebaseFirestore.getInstance();
        currentUser  = FirebaseAuth.getInstance().getCurrentUser ();

        // Load current user data from Firestore
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    emailEditText.setText(documentSnapshot.getString("email"));
                    mobileEditText.setText(documentSnapshot.getString("mobile"));
                    adressEditText.setText(documentSnapshot.getString("address")); // Load address
                    String photoUrl = documentSnapshot.getString("photoUrl");
                    if (photoUrl != null) {
                        new LoadProfileImageTask(profileImageView).execute(photoUrl);
                    }
                }
            });
        }

        // Set click listener for the profile image
        profileImageView.setOnClickListener(v -> openGallery());
        saveButton.setOnClickListener(v -> saveProfileData());

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
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String mobile = mobileEditText.getText().toString();
        String address = adressEditText.getText().toString(); // Get the address
        String photoUrl = null; // Assume you have logic to get the new photo URL if selected

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("mobile", mobile);
            userData.put("address", address); // Include address in the data
            userData.put("photoUrl", photoUrl); // Store photoUrl (can be null if no photo is selected)

            firestore.collection("users").document(userId).set(userData).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                // Navigate back to ProfileViewFragment after saving data
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.ProfileVeiwFragment);
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
            });
        }
    }


    private void updateUserData(String name, String email, String mobile, String address, String photoUrl) {
        // Prepare data to save to Firestore
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("mobile", mobile);
            userData.put("address", address); // Save address
            userData.put("photoUrl", photoUrl); // Save photoUrl (can be null if no image selected)

            // Update the user data in Firestore
            firestore.collection("users").document(userId).set(userData).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                // Optionally navigate back to HomeFragment
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // AsyncTask to load the profile image from URL
    private static class LoadProfileImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadProfileImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream inputStream;
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();
                inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.baseline_person_24); // Default image if loading fails
            }
        }
    }
}
