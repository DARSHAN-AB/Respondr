package com.example.respondr.ui.home;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.respondr.R;

public class IncidentReportFragment extends Fragment {

    private TextView title;
    private TextView currentLocation;
    private Spinner incidentTypeSpinner;
    private EditText description;
    private TextView addAttachmentsText;
    private Button sendReportButton;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_incident_report, container, false);
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = view.findViewById(R.id.tv_report_title);
        currentLocation = view.findViewById(R.id.et_current_location);
        incidentTypeSpinner = view.findViewById(R.id.spinner_incident_type);
        description = view.findViewById(R.id.et_description);
        addAttachmentsText = view.findViewById(R.id.tv_add_attachments);
        sendReportButton = view.findViewById(R.id.btn_send_report);

        // Retrieve incident type from the bundle
        if (getArguments() != null) {
            String incidentType = getArguments().getString("incident_type", "Accident");
            setSpinnerSelection(incidentType);
        }

        // Set click listeners
        addAttachmentsText.setOnClickListener(v -> openGalleryOrCamera());
        sendReportButton.setOnClickListener(v -> sendReport());

        // Fetch and set the location from SharedPreferences directly
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String location = sharedPreferences.getString("current_location", "Location not available");

        // Set the location text in the TextView
        currentLocation.setText(location);
    }

    private void openGalleryOrCamera() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");  // You can modify this to allow videos or images
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void setSpinnerSelection(String incidentType) {
        // Ensure the context is available
        if (getContext() == null) return;

        // Set the adapter for the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.incident_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        incidentTypeSpinner.setAdapter(adapter);

        // Set selection based on the passed incidentType
        int selection = 0; // Default to the first item

        switch (incidentType) {
            case "Accident":
                selection = 0;
                break;
            case "Fire emergency":
                selection = 1;
                break;
            case "street fight(call police)": // Ensure this matches exactly
                selection = 2;
                break;
            default:
                Log.d("IncidentReport", "Unknown incident type: " + incidentType);
                // Optionally, display a toast or handle this case differently
                break;
        }

        // Set the selected item
        incidentTypeSpinner.setSelection(selection);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get the image URI
            Uri selectedImageUri = data.getData();

            // Get the file name of the selected image
            String fileName = getFileNameFromUri(selectedImageUri);

            // Update the TextView with the file name
            TextView addAttachmentsText = getView().findViewById(R.id.tv_add_attachments);
            addAttachmentsText.setText(fileName);  // Show the file name in the TextView
        }
    }

    // Function to get the file name from the URI
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            fileName = cursor.getString(columnIndex);
            cursor.close();
        }

        return fileName;
    }

    private void sendReport() {
        // Logic to send the report
        String location = currentLocation.getText().toString();
        String incidentType = incidentTypeSpinner.getSelectedItem().toString();
        String incidentDescription = description.getText().toString();

        // Here, you could send the data to a server or database
        // For now, just log the details or show a toast

        // Example: Toast.makeText(requireContext(), "Report sent!", Toast.LENGTH_SHORT).show();
    }
}
