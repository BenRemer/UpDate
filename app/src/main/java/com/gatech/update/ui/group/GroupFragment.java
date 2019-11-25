package com.gatech.update.ui.group;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gatech.update.Controller.DeleteActivity;
import com.gatech.update.Controller.DrawerActivity;
import com.gatech.update.Controller.GroupStructure;
import com.gatech.update.Controller.InviteUserActivity;
import com.gatech.update.R;

import java.util.ArrayList;

public class GroupFragment extends Fragment {

    private LinearLayout.LayoutParams params;
    private LinearLayout ll;
    private TextView text_Name, text_Status;
    private String groupID, groupName;

    private Button buttonDelete, buttonInvite;

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group, container, false);

        // Fetches content from item user clicks on
        Bundle bundle = this.getArguments();
        GroupStructure group = bundle.getParcelable("Group");

        ArrayList<String> users = group.getUsers();
        ArrayList<String> status = group.getStatus();
        groupName = group.getGroupName();
        groupID = group.getGroupID();

        // Set title to reflect groups name
        ((DrawerActivity) getActivity()).setActionBarTitle(groupName);

        ll = root.findViewById(R.id.l_layout);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        buttonDelete = root.findViewById(R.id.button_delete);
        buttonInvite = root.findViewById(R.id.button_invite);

        // We wish to dynamically add information of each user inside the group
        Context ctx = root.getContext();
        for (int i = 0; i < users.size(); i++) {
            // Create
            text_Name = new TextView(ctx);
            text_Status = new TextView(ctx);
            // Params
            text_Name.setLayoutParams(params);
            text_Name.setTextSize(24);
            text_Name.setTextColor(Color.BLACK);
            text_Status.setLayoutParams(params);
            text_Status.setTextSize(18);
            // Set the text
            text_Name.setText(users.get(i));
            text_Status.setText(status.get(i));
            // Add views
            this.ll.addView(text_Name);
            this.ll.addView(text_Status);
        }

        // onClick when user prompts to delete group
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the intent for activity & Pass groupID
                Intent mIntent = new Intent(getActivity(), DeleteActivity.class);
                mIntent.putExtra("ID", groupID);

                // Pull up confirmation window to delete group
                startActivity(mIntent);
            }
        });

        // onClick when user prompts to add user
        buttonInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the intent for activity & Pass groupID
                Intent mIntent = new Intent(getActivity(), InviteUserActivity.class);
                mIntent.putExtra("ID", groupID);
                mIntent.putExtra("Name", groupName);

                // Pull up confirmation window to delete group
                startActivity(mIntent);
            }
        });

        return root;
    }


}
