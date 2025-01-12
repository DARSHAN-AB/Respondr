package com.example.respondr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ResponderProfileEditActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private EditText editName, editEmail;
    private Spinner spinnerRole;
    private Button btnSaveProfile;
    private Uri idProofUri;
    private TextView tvIDProofFileName;

    private static final int PICK_FILE_REQUEST = 1; // Request code for file selection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responder_profile_edit);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI elements
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        tvIDProofFileName = findViewById(R.id.tv_add_attachmentsr);

        // Set up the roles in the Spinner
        setUpRoleSpinner();

        // Set click listener to open gallery or file manager
        tvIDProofFileName.setOnClickListener(v -> openFilePicker());

        // Set click listener to save profile data
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        // Prefill the data if available
        prefillData();
    }

    private void setUpRoleSpinner() {
        // Define roles to show in the spinner
        String[] roles = new String[]{"Ambulance", "Fire", "Police"};

        // Set up the ArrayAdapter with the roles
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);

        // Set the adapter to the spinner
        spinnerRole.setAdapter(adapter);
    }

    private void prefillData() {
        // Fetch current user data from Firestore
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String role = documentSnapshot.getString("role");
                        String idProofUrl = documentSnapshot.getString("idProofUrl");

                        editName.setText(name);
                        editEmail.setText(email);

                        // Set the role in the spinner (you can improve this logic based on your requirement)
                        setRoleInSpinner(role);

                        // Show the file name of the uploaded ID proof if exists
                        if (idProofUrl != null && !idProofUrl.isEmpty()) {
                            String fileName = idProofUrl.substring(idProofUrl.lastIndexOf("/") + 1);
                            tvIDProofFileName.setText("ID Proof: " + fileName);
                        }
                    }
                });
    }

    private void setRoleInSpinner(String role) {
        // Set the role in the spinner from Firestore data (this part can be customized)
        String[] roles = new String[]{"Ambulance", "Fire", "Police"};
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(role)) {
                spinnerRole.setSelection(i);
                break;
            }
        }
    }

    private void openFilePicker() {
        // Open file picker using Storage Access Framework
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");  // You can specify specific types like "application/pdf", "image/*", etc.
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    // Handle the result of the file picker (onActivityResult)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            idProofUri = data.getData();  // Get the URI of the selected file
            String fileName = idProofUri.getLastPathSegment();
            tvIDProofFileName.setText("ID Proof: " + fileName);
            Toast.makeText(this, "ID Proof Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        String name = editName.getText().toString();
        String email = editEmail.getText().toString();
        String role = spinnerRole.getSelectedItem().toString();

        if (idProofUri == null) {
            Toast.makeText(this, "ID Proof is mandatory", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Upload the ID proof to Firebase Storage
        StorageReference idProofRef = storage.getReference().child("idProofs/" + System.currentTimeMillis() + ".pdf");
        idProofRef.putFile(idProofUri).addOnSuccessListener(taskSnapshot -> {
            idProofRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String idProofUrl = uri.toString();

                // Upload the profile data to Firestore
                updateFirestore(name, email, role, idProofUrl);

                progressDialog.dismiss();
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to upload ID Proof", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFirestore(String name, String email, String role, String idProofUrl) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("name", name);
        profileData.put("email", email);
        profileData.put("role", role);
        profileData.put("idProofUrl", idProofUrl);  // Update ID proof URL in Firestore

        firestore.collection("users").document(auth.getCurrentUser().getUid())
                .update(profileData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ResponderProfileActivity.class));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                });
    }
}
