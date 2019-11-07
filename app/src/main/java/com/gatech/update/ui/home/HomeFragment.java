package com.gatech.update.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gatech.update.Controller.CreateGroupActivity;
import com.gatech.update.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ScrollView myGroups;

    // Relating to current CardView items
    private CardView group_card;
    private CardView.LayoutParams group_card_params;
    private TextView group_name;
    private TextView group_user_name;
    private TextView group_user_status;

    private ArrayList<String> groupNameList = new ArrayList<>();

    private LinearLayout ll;
    private LinearLayout.LayoutParams params;

    private FirebaseUser mUser;

    private String TAG = "DisplayGroupInfo";

    int currGroupNum;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        currGroupNum = 1;

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
        }

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get list of groups
        myGroups = root.findViewById(R.id.groupInfo);
        ll = root.findViewById(R.id.ll);

        // Define layout params for each object
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        group_card_params = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        group_card_params.setMargins(0, 0, 0,10);

        db.collection("Users")
                .document(mUser.getUid())
                .collection("Groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> groupTask) {
                        if (groupTask.isSuccessful()) {
                            for (QueryDocumentSnapshot doc_group : groupTask.getResult()) {
                                // We must dynamically populate home screen with group information
                                // for every group user is a part of

                                // 1. Create a new card view to contain current group
                                group_card = new CardView(getContext());
                                group_name = new TextView(getContext());
                                group_name.setText(doc_group.getString("Group_Name"));

                                // Editing params
                                group_name.setTextSize(20);
                                group_name.setTextColor(Color.WHITE);
                                group_card.setCardBackgroundColor(Color.rgb(0, 133, 120));
                                group_card.setContentPadding(8, 5, 8, 5);

                                // Create the hierarchy
                                // [ Linear Layout
                                //    [ Card
                                //       [ Group-specific info
                                //   ...
                                ll.addView(group_card, group_card_params);
                                group_card.addView(group_name, params);

                                // 2. Add Users
                                db.collection("Groups")
                                        .document(Objects.requireNonNull(doc_group.getString("Group_ID")))
                                        .collection("Users")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> userTask) {
                                                if (userTask.isSuccessful()) {
                                                    for (QueryDocumentSnapshot doc_user : userTask.getResult()) {
                                                        // 2. Add Users
                                                        group_user_name.setText(doc_user.getString("Display_Name"));
                                                        group_card.addView(group_user_name, params);

                                                        // 3. Add Status of each user
//                                                        group_user_status.setText(db.collection("Users")
//                                                                .document(doc_user.getString("Firebase_ID"))
//                                                                .get()
//                                                                .getResult().getString("Status"));
                                                    }
                                                    Log.d(TAG, "Success: Retrieved users.");
                                                } else {
                                                    Log.d(TAG, "Error retrieving user docs");
                                                }
                                            }
                                        });


                                // Dynamically adds buttons to the home screen of all groups user is
                                // a part of
//                                desiredGroup = new Button(getContext());
//                                desiredGroup.setText(doc_group.getData().get("Group_Name").toString());
//                                ll.addView(desiredGroup, params);

                                /* TODO: Display Information of each group (Names of those in each
                                         group & their status -> 2+ ways to implement)
                                         1 - The "GroupMe" method (Buttons bring you to separate
                                             screen & display information there)
                                         2 - The "Facebook" method (all information on a scrolling
                                             homepage - no need for buttons, could change to text)
                                 */

                            }
                            Log.d(TAG, "Success: Retrieved groups.");
                        } else {
                            Log.d(TAG, "Error retrieving group docs.");
                        }
                    }
                });

        // Should pull information based on groups of user
        // User should be able to start a new group from start
        final Button newGroupButton = root.findViewById(R.id.create_group_b);
        newGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Begins the new activity (Overlay create group pane)
                startActivity(new Intent(getActivity(), CreateGroupActivity.class));
            }
        });

        return root;
    }
}