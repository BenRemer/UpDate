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
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gatech.update.Controller.DeleteActivity;
import com.gatech.update.Controller.DrawerActivity;
import com.gatech.update.Controller.GroupStructure;
import com.gatech.update.Controller.InviteUserActivity;
import com.gatech.update.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class GroupFragment extends Fragment {

    private LinearLayout.LayoutParams params;
    private LinearLayout ll;
    private TextView text_Name, text_Status, text_Activity;
    private String groupID, groupName;
    private TableRow tr;

    private Button buttonDelete, buttonInvite;

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth user = FirebaseAuth.getInstance();
        // Fetches content from item user clicks on
        Bundle bundle = this.getArguments();
        GroupStructure group = bundle.getParcelable("Group");

        ArrayList<String> users = group.getUsers();
        ArrayList<String> status = group.getStatus();
        ArrayList<String> activities = group.getActivity();
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
        buttonDelete.setVisibility(View.GONE); // Defaults to button being gone
        buttonInvite = root.findViewById(R.id.button_invite);
        // Find if user is the owner of the group, if so allow them to delete the group
        db.collection("Groups")
                .document(groupID)
                .collection("Users")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                           if (task.isSuccessful()) {
                               DocumentSnapshot document = task.getResult();
                               if (document != null) {
                                   String permission = document.getString("Permission");
                                   if (permission.equals("Owner")) {
                                       buttonDelete.setVisibility(View.VISIBLE);
                                   }
                               }
                           }
                       }
                   });
        // We wish to dynamically add information of each user inside the group
        Context ctx = root.getContext();
        for (int i = 0; i < users.size(); i++) {
            // Create
            text_Name = new TextView(ctx);
            text_Status = new TextView(ctx);
            text_Activity = new TextView(ctx);
            tr = new TableRow(ctx);

            // Params
            text_Name.setLayoutParams(params);
            text_Name.setTextSize(24);
            text_Name.setTextColor(Color.BLACK);

            text_Status.setLayoutParams(params);
            text_Status.setTextSize(18);

            text_Activity.setLayoutParams(params);
            text_Activity.setTextSize(18);

            // Set the text
            text_Name.setText(users.get(i));
            text_Status.setText(status.get(i));
            text_Activity.setText(activities.get(i));

            // Add views
//            this.tr.addView(text_Name);
//            this.tr.addView(text_Activity);
            this.ll.addView(text_Name);
            this.ll.addView(text_Activity);
            this.ll.addView(text_Status);

//            tr.setLayoutParams(params);
//            tr.setOrientation(LinearLayout.HORIZONTAL);
        }
//        root.setFocusableInTouchMode(true);
//        root.requestFocus();
//        root.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.i("Group", "keyCode: " + keyCode);
//                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
//                    Log.i("Group", "onKey Back listener is working!!!");
//                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                    return true;
//                }
//                return false;
//            }
//        });

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
