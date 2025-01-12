package com.example.respondr.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.respondr.R;
import com.example.respondr.ui.home.EmergencyAdapter;
import com.example.respondr.ui.home.EmergencyItem;

import java.util.ArrayList;
import java.util.List;

public class EmergencyFragment extends Fragment {

    private RecyclerView recyclerView;
    private EmergencyAdapter emergencyAdapter;
    private List<EmergencyItem> emergencyList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emergency_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_emergency_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize data
        emergencyList = new ArrayList<>();
        populateEmergencyList();

        // Set up the adapter
        emergencyAdapter = new EmergencyAdapter(emergencyList, this::onEmergencyItemClick);
        recyclerView.setAdapter(emergencyAdapter);
    }

    private void populateEmergencyList() {
        // Add emergency items
        emergencyList.add(new EmergencyItem(R.drawable.accident, "An accident"));
        emergencyList.add(new EmergencyItem(R.drawable.fire, "Fire emergency"));
        emergencyList.add(new EmergencyItem(R.drawable.siren, "street fight(call police)"));
    }

    private void onEmergencyItemClick(EmergencyItem emergencyItem) {
        // Check the emergency item clicked
        if ("An accident".equals(emergencyItem.getEmergencyName())) {
            // Check the emergency item clicked
            Bundle bundle = new Bundle();
            bundle.putString("incident_type", emergencyItem.getEmergencyName());
            // Assuming you're using NavController for navigation
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.incidentReportFragment, bundle);
        } else if ("Fire emergency".equals(emergencyItem.getEmergencyName())) {
            // Check the emergency item clicked
            Bundle bundle = new Bundle();
            bundle.putString("incident_type", emergencyItem.getEmergencyName());
            // Assuming you're using NavController for navigation
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.incidentReportFragment, bundle);
        } else if ("street fight(call police)".equals(emergencyItem.getEmergencyName())) {
            // Check the emergency item clicked
            Bundle bundle = new Bundle();
            bundle.putString("incident_type", emergencyItem.getEmergencyName());
            // Assuming you're using NavController for navigation
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.incidentReportFragment, bundle);
        }
    }
}
