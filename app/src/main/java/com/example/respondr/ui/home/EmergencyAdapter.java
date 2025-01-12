package com.example.respondr.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.respondr.R;
import com.example.respondr.ui.home.EmergencyItem;

import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder> {

    private final List<EmergencyItem> emergencyList;
    private final OnEmergencyClickListener listener;

    public interface OnEmergencyClickListener {
        void onEmergencyClick(EmergencyItem emergencyItem);
    }

    public EmergencyAdapter(List<EmergencyItem> emergencyList, OnEmergencyClickListener listener) {
        this.emergencyList = emergencyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency, parent, false);
        return new EmergencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyViewHolder holder, int position) {
        EmergencyItem item = emergencyList.get(position);
        holder.icon.setImageResource(item.getIconResId());
        holder.title.setText(item.getEmergencyName());

        holder.itemView.setOnClickListener(v -> listener.onEmergencyClick(item));
    }

    @Override
    public int getItemCount() {
        return emergencyList.size();
    }

    static class EmergencyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        EmergencyViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.img_emergency_icon);
            title = itemView.findViewById(R.id.tv_emergency_name);
        }
    }
}
