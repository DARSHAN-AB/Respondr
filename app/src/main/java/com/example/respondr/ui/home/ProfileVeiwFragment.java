package com.example.respondr.ui.home;

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

public class ProfileVeiwFragment extends Fragment {

    private TextView nameTextView, mobileTextView, emailTextView;
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

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String photoUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null;

            // Set profile image and name
            if (name != null) {
                nameTextView.setText(name);
            } else {
                nameTextView.setText("Add your name");
            }

            // Check if the user has a profile image URL and set it, or set a default image
            if (photoUrl != null) {
                // Use a default image for profile if no photo URL is available
                profileImageView.setImageURI(currentUser.getPhotoUrl());
            } else {
                profileImageView.setImageResource(R.drawable.baseline_person_24); // Default image
            }

            // Retrieve additional details (email, phone) from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");

                    emailTextView.setText(email != null ? email : "Add your email");
                    mobileTextView.setText(phone != null ? phone : "Add your phone number");
                }
            });
        }

        // Navigate to Edit Fragment when the button is clicked
        editProfileButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.profileEditFragment);
        });

        return view;
    }
}

