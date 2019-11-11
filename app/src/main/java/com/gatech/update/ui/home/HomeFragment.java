package com.gatech.update.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gatech.update.Controller.CreateGroupActivity;
import com.gatech.update.Controller.GroupStructure;
import com.gatech.update.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private FirebaseUser mUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // recyclerView items
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    // define a list of groups objects
    private ArrayList<GroupStructure> mGroups;
    // now a list for users in a certain group & their status...
    private ArrayList<String> mGroupNames;
    private ArrayList<String> mGroupIDs;
    private ArrayList<String> mUsers;
    private ArrayList<String> mStatus;

    // create tag for debugging
    private String TAG = "DisplayGroupInfo";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);

        // create the group list in memory
        mGroups = new ArrayList<>();
        mGroupNames = new ArrayList<>();
        mGroupIDs = new ArrayList<>();
        mUsers = new ArrayList<>();
        mStatus = new ArrayList<>();

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        // Obtain current user information
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        // User should be able to start a new group from start
        final Button newGroupButton = root.findViewById(R.id.create_group_b);
        newGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Begins the new activity (Overlay create group pane)
                startActivity(new Intent(getActivity(), CreateGroupActivity.class));
            }
        });

        // Read Database information about User's groups
        // 1: Acquire a List of Group Names
        readGroupNames(new listCallback() {
            @Override
            public void onCallback(ArrayList<String> groupNames) {
                Log.d(TAG, "=DEBUG= Callback Groups: " + groupNames.toString());

                // 2: Acquire a List of User Names (per group)
                for (int i = 0; i < mGroupNames.size(); i++) {
                    // Clears users & status lists (Different for each group)
                    final String groupName = mGroupNames.get(i);
                    readUserNames(new listCallback() {
                        @Override
                        public void onCallback(ArrayList<String> userNames) {
                            Log.d(TAG, "=DEBUG= Callback from User Names");

                            // Add to structure
                            mGroups.add(new GroupStructure(groupName, mUsers, mStatus));

                            // At end, we can finally add the data to our recycler view
                            mRecyclerView = root.findViewById(R.id.rview);
                            // this line increases performance & doesn't change size on num of items
                            mRecyclerView.setHasFixedSize(true);
                            mLayoutManager = new LinearLayoutManager(getActivity());
                            mAdapter = new GroupAdapter(mGroups);

                            mRecyclerView.setLayoutManager(mLayoutManager);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    }, mGroupIDs.get(i), mGroupNames.get(i));
                }
            }
        });
        Log.d(TAG, "=DEBUG= Returning root & drawing content");
        return root;
    }

    private void readGroupNames(final listCallback callback) {
        db.collection("Users")
                .document(mUser.getUid())
                .collection("Groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> groupTask) {
                        if (groupTask.isSuccessful()) {
                            String name, ID;
                            for (QueryDocumentSnapshot doc_group : groupTask.getResult()) { // Each Group a user is connected to
                                // we now have the group's name & ID
                                name = doc_group.getString("Group_Name");
                                mGroupNames.add(name);
                                ID = doc_group.getString("Group_ID");
                                mGroupIDs.add(ID);
                            }
                        } else {
                            Log.d(TAG, "=DEBUG= Error retrieving group docs.");
                        }
                        callback.onCallback(mGroupNames);
                    }
                });
    }

    private void readUserNames(final listCallback callback, String ID, final String name) {
        Log.d(TAG, "=DEBUG= \tPerforming lookup on " + ID);
        db.collection("Groups")
                .document(ID)
                .collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> userTask) {
                        if (userTask.isSuccessful()) {
                            String user, status;
                            mUsers.clear();
                            mStatus.clear();
                            for (QueryDocumentSnapshot doc_user : userTask.getResult()) {
                                // we now have each of the users' names & status
                                user = doc_user.getString("Display_Name");
                                status = doc_user.getString("Status");

                                // Add to respective lists
                                mUsers.add(user);
                                if (status != null) {
                                    mStatus.add(status);
                                } else {
                                    mStatus.add("");
                                }
                                Log.d(TAG, "=DEBUG= \t\tFound user [" + user + "] : " + status);
                            }
                            Log.d(TAG, "=DEBUG= \tSuccess: Retrieved users.");
                        } else {
                            Log.d(TAG, "=DEBUG= \tError retrieving user docs");
                        }
                        callback.onCallback(mUsers);
                    }
                });
    }

    public interface listCallback {
        void onCallback(ArrayList<String> data);
    }

}