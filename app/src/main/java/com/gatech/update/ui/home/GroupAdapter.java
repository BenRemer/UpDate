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

    public static class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mGroupName;
        public TextView mUserNames;
        public TextView mStatus;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            mGroupName = itemView.findViewById(R.id.groupName);
            mUserNames = itemView.findViewById(R.id.userNames);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // TODO: implement onClick functionality for each group
            //      should open up a window relating to each group specifics
            //      should be simple as we have access to the data in each
            //      & can call it by .get(position)


        }
    }

    public GroupAdapter(ArrayList<GroupStructure> groups) {
        mGroups = groups;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_group, parent, false);
        GroupViewHolder gHolder = new GroupViewHolder(v);
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
