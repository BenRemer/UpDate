package com.gatech.update.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gatech.update.Controller.GroupStructure;
import com.gatech.update.R;

import java.util.ArrayList;
import java.util.Arrays;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private ArrayList<GroupStructure> mGroups;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        public TextView mGroupName;
        public TextView mUserNames;
        public TextView mStatus;

        public GroupViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mGroupName = itemView.findViewById(R.id.groupName);
            mUserNames = itemView.findViewById(R.id.userNames);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        // Grab position
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onItemClick(pos);
                        }
                    }
                }
            });
        }

    }

    public GroupAdapter(ArrayList<GroupStructure> groups) {
        mGroups = groups;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_group, parent, false);
        GroupViewHolder gHolder = new GroupViewHolder(v, mListener);
        return gHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupStructure currentGroup = mGroups.get(position);

        holder.mGroupName.setText(currentGroup.getGroupName());
        holder.mUserNames.setText(Arrays.toString(currentGroup.getUsers().toArray()));
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }
}
