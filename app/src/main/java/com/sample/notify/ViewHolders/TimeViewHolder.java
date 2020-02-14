package com.sample.notify.ViewHolders;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.notify.R;

import java.time.temporal.Temporal;

public class TimeViewHolder extends RecyclerView.ViewHolder{

    public CardView cardView;
    public TextView txt_time_slot;

    public TimeViewHolder(@NonNull CardView itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.card_time_slot);
        txt_time_slot = itemView.findViewById(R.id.txt_time_slot);
    }
}
