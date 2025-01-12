package com.example.respondr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ResponderProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ImageView profileImage;
    private TextView tvName, tvEmail, tvRole;
    private Button btnEditProfile;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        profileImage = findViewById(R.id.profileImage);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        userId = auth.getCurrentUser().getUid();

        loadProfileData();

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResponderProfileEditActivity.class);
            startActivity(intent);
        });
    }

    private void loadProfileData() {
        DocumentReference userDoc = firestore.collection("users").document(userId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String role = documentSnapshot.getString("role");
                String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                tvName.setText("Name: " + name);
                tvEmail.setText("Email: " + email);
                tvRole.setText("Role: " + role);

                if (profileImageUrl != null) {
                    loadImageFromUrl(profileImageUrl); // Use built-in method to load image
                } else {
                    profileImage.setImageResource(R.drawable.baseline_person_24); // Default image if no URL
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ResponderProfileActivity.this, "Error loading profile data.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadImageFromUrl(String url) {
        StorageReference profileRef = storage.getReferenceFromUrl(url);
        profileRef.getBytes(1024 * 1024) // Max 1MB
                .addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    profileImage.setImageBitmap(bitmap);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ResponderProfileActivity.this, "Failed to load profile image.", Toast.LENGTH_SHORT).show();
                });
    }
}
