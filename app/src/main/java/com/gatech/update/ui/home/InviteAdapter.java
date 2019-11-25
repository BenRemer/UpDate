package com.gatech.update.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gatech.update.Controller.InviteStructure;
import com.gatech.update.R;

import java.util.ArrayList;
import java.util.Arrays;

public class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.InviteViewHolder> {

    private ArrayList<InviteStructure> mInvites;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onConfirmClick(int position);
        void onDeclineClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class InviteViewHolder extends RecyclerView.ViewHolder {
        public TextView mGroupName;
        public TextView mHostName;
        public ImageButton mConfirm, mDecline;

        public InviteViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mGroupName = itemView.findViewById(R.id.groupName);
            mHostName = itemView.findViewById(R.id.hostName);
            mConfirm = itemView.findViewById(R.id.acceptInvite);
            mDecline = itemView.findViewById(R.id.declineInvite);

            mConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        // Grab position
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onConfirmClick(pos);
                        }
                    }
                }
            });

            mDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        // Grab position
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onDeclineClick(pos);
                        }
                    }
                }
            });
        }

    }


    public InviteAdapter(ArrayList<InviteStructure> invites) {
        mInvites = invites;
    }

    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_invite, parent, false);
        InviteViewHolder vHolder = new InviteViewHolder(v, mListener);
        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull InviteViewHolder holder, int position) {
        InviteStructure currentInvite = mInvites.get(position);

        holder.mGroupName.setText(currentInvite.getGroupName());
        holder.mHostName.setText(currentInvite.getHostName());
    }

    @Override
    public int getItemCount() {
        return mInvites.size();
    }
}
