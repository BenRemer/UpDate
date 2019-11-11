package com.gatech.update.Controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gatech.update.R;

import java.util.ArrayList;

public class GroupActivity extends AppCompatActivity {

//    private ArrayList<String> users, status;
//    private String groupName;

    private LinearLayout.LayoutParams params;
    private LinearLayout ll;
    private TextView text_Name, text_Status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // TODO: Add the toolbar to the groupActivity

        Intent intent = getIntent();
        // Grabs the item clicked as the name "Group"
        GroupStructure group = intent.getParcelableExtra("Group");

        // Fetches content from item user clicks on
        ArrayList<String> users = group.getUsers();
        ArrayList<String> status = group.getStatus();
        String groupName = group.getGroupName();
        setTitle(groupName);

        ll = this.findViewById(R.id.l_layout);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // We wish to dynamically add information of each user inside the group
        for (int i = 0; i < users.size(); i++) {
            // Create
            text_Name = new TextView(this);
            text_Status = new TextView(this);
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

    }
}
