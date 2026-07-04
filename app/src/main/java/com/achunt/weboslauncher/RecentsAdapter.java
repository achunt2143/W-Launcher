package com.achunt.weboslauncher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.MyViewHolder> {

    public List<AppInfo> dataSet;

    public RecentsAdapter(List<AppInfo> data) {
        this.dataSet = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        //updateData(dataSet);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recents_cards, parent, false);

        view.setOnClickListener(new HomeScreenK.RecentsClickListener());
        view.setOnLongClickListener(new HomeScreenK.RecentsLongClickListener());

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        AppInfo app = dataSet.get(listPosition);
        holder.textViewName.setText(app.label);
        holder.imageViewIcon.setImageDrawable(app.icon);
        holder.imageViewIconSmall.setImageDrawable(app.icon);
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        ImageView imageViewIcon;
        ImageView imageViewIconSmall;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = itemView.findViewById(R.id.recentName);
            this.imageViewIcon = itemView.findViewById(R.id.recentIcon);
            this.imageViewIconSmall = itemView.findViewById(R.id.recentIconSmall);
        }
    }


}
