package com.example.respondr.ui.home;

public class EmergencyItem {
    private int iconResId;
    private String emergencyName;

    public EmergencyItem(int iconResId, String emergencyName) {
        this.iconResId = iconResId;
        this.emergencyName = emergencyName;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getEmergencyName() {
        return emergencyName;
    }
}

