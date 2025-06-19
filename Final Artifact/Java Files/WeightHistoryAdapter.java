package com.example.weighttracking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.WeightHistoryViewHolder> {

    private final List<WeightEntry> weightEntries;
    private final OnEditListener editListener;
    private final OnDeleteListener deleteListener;

    // constructor with listeners
    public WeightHistoryAdapter(List<WeightEntry> weightEntries, OnEditListener editListener, OnDeleteListener deleteListener) {
        this.weightEntries = weightEntries;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    // listener for edit actions
    public interface OnEditListener {
        void onEdit(int entryId, String date, String weight);
    }

    // listener for delete actions
    public interface OnDeleteListener {
        void onDelete(int entryId);
    }

    @NonNull
    @Override
    public WeightHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weight_entry, parent, false);
        return new WeightHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightHistoryViewHolder holder, int position) {
        WeightEntry entry = weightEntries.get(position);

        String formattedDate = DateUtils.formatDateForDisplay(entry.getDate());
        holder.entryDate.setText(holder.itemView.getContext().getString(R.string.weight_entry_date, formattedDate));
        holder.entryWeight.setText(holder.itemView.getContext().getString(R.string.weight_entry_weight, entry.getWeight()));

        // edit Button Click Listener
        holder.editButton.setOnClickListener(v -> editListener.onEdit(entry.getId(), entry.getDate(), String.valueOf(entry.getWeight())));

        // delete Button Click Listener
        holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(entry.getId()));
    }


    @Override
    public int getItemCount() {
        return weightEntries.size();
    }

    public static class WeightHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView entryDate;
        private final TextView entryWeight;
        private final View editButton;
        private final View deleteButton;

        WeightHistoryViewHolder(View itemView) {
            super(itemView);
            entryDate = itemView.findViewById(R.id.entryDate);
            entryWeight = itemView.findViewById(R.id.entryWeight);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}