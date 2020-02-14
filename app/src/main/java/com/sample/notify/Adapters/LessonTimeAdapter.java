package com.sample.notify.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.notify.R;
import com.sample.notify.ViewHolders.TimeViewHolder;

public class LessonTimeAdapter extends RecyclerView.Adapter<TimeViewHolder>{
    private Context context;
    private String time;
    private String[] time2;
    public static final String KEY_BUTTON_ENABLE = "KEY_BUTTON_ENABLE";
    public static final String KEY_SELECTED = "KEY_SELECTED";
    LocalBroadcastManager localBroadcastManager;
    String name="";

    public LessonTimeAdapter(Context context, String time) {
        this.context = context;
        this.time = time;
        time2 = time.split(",");
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_slot_time, parent, false);
        return new TimeViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int i) {
        holder.txt_time_slot.setText(time2[i]);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KEY_BUTTON_ENABLE);
                intent.putExtra(KEY_SELECTED,holder.txt_time_slot.getText());
                localBroadcastManager.sendBroadcast(intent);

                if(name.isEmpty()) {
                    holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                    name = holder.txt_time_slot.getText().toString();
                }else{
                    holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                    if (name.equals(holder.txt_time_slot.getText().toString())){
                        name="";
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return time2.length;
    }

}
