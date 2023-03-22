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

        TextView textViewName = holder.textViewName;
        //TextView textViewVersion = holder.textViewVersion;
        ImageView imageView = holder.imageViewIcon;
        textViewName.setText(dataSet.get(listPosition).label);
        //textViewName.setTag(dataSet.get(listPosition).packageName);
        //textViewVersion.setText(dataSet.get(listPosition));
        imageView.setImageDrawable(dataSet.get(listPosition).icon);
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewVersion;
        ImageView imageViewIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewName = itemView.findViewById(R.id.recentName);
            //this.textViewVersion = (TextView) itemView.findViewById(R.id.textViewVersion);
            this.imageViewIcon = itemView.findViewById(R.id.recentIcon);
        }
    }


}
