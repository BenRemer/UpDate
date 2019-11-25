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

    /** Takes in a list of names & outputs a string of the first names
     *
     * @param list list of user names
     * @return string of first names, delimited by commas
     */
    private String parseFirstNames(ArrayList<String> list) {
        String out = "";
        int size = list.size();
        int index;

        if (size == 1) {
            index = list.get(0).indexOf(' ');
            return list.get(0).substring(0, index);
        } else if (size == 2) {
            index = list.get(0).indexOf(' ');
            out = out + list.get(0).substring(0, index) + " ";
        } else {
            for (int i = 0; i < size - 1; i++) {
                index = list.get(i).indexOf(' ');
                out = out + list.get(i).substring(0, index) + ", ";
            }
        }
        index = list.get(size - 1).indexOf(' ');
        out = out + "& " + list.get(size - 1).substring(0, index);

        return out;
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
        String usernames;

        holder.mGroupName.setText(currentGroup.getGroupName());
        // Obtain list of usernames
        usernames = "with " + parseFirstNames(currentGroup.getUsers()) + ".";
        holder.mUserNames.setText(usernames);
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }
}
