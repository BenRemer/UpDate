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
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gatech.update.Controller.CreateGroupActivity;
import com.gatech.update.Controller.DrawerActivity;
import com.gatech.update.Controller.GroupStructure;
import com.gatech.update.Controller.InviteStructure;
import com.gatech.update.R;
import com.gatech.update.ui.group.GroupFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private FirebaseUser mUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // etc. items
    private TextView inviteNotification;
    private View line;

    // Information regarding accepted group
    private InviteStructure invGroup;
    private String invGroupID, invGroupName;
    private HashMap<String, Object> hashGroup, hashUser;

    // recyclerView items
    private RecyclerView mRVGroup, mRVInv;
    private GroupAdapter mAdapterGroup;
    private InviteAdapter mAdapterInv;
    private RecyclerView.LayoutManager mLMGroup, mLMInv;

    // define a list of groups objects
    private ArrayList<GroupStructure> mGroups;
    // define a list of invite objects
    private ArrayList<InviteStructure> mInvites;
    // now a list for users in a certain group & their status...
    private ArrayList<String> mGroupNames, mGroupIDs, mUsers, mStatus;

    // create tag for debugging
    private String TAG = "DisplayGroupInfo";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        // create the group lists in memory
        mGroups = new ArrayList<>();
        mInvites = new ArrayList<>();

        // Set title to reflect group name
        ((DrawerActivity) getActivity()).setActionBarTitle("Your Groups");

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

        // Set up items in display
        inviteNotification = root.findViewById(R.id.text_notification);
        line = root.findViewById(R.id.hzLine);
        mRVInv = root.findViewById(R.id.rInvites);
        mRVGroup = root.findViewById(R.id.rGroups);
        // & Layout managers
        mLMInv = new LinearLayoutManager(getContext());
        mLMGroup = new LinearLayoutManager(getContext());

        // Read Database information to check for invites
        // 1: Acquire a List of Invites
        readInvites(new inviteCallback() {
            @Override
            public void onCallback(ArrayList<InviteStructure> inviteList) {
                Log.d(TAG, "=DEBUG= Callback Invites");
                int numInvites = inviteList.size();

                // If list is not empty, add cards to view
                if (numInvites != 0) {
                    Log.d(TAG, "=DEBUG= invite list size of " + inviteList.size());
                    inviteNotification.setVisibility(View.VISIBLE);
                    line.setVisibility(View.VISIBLE);
                    if (numInvites == 1)
                        inviteNotification.setText(numInvites + " new group invitation!");
                    else
                        inviteNotification.setText(numInvites + " new group invitations!");

                    mAdapterInv = new InviteAdapter(inviteList);

                    mRVInv.setLayoutManager(mLMInv);
                    mRVInv.setAdapter(mAdapterInv);

                    // Create onClick Listener
                    mAdapterInv.setOnItemClickListener(new InviteAdapter.OnItemClickListener() {

                        @Override
                        public void onConfirmClick(int position) {
                            // Updates database to bring you in the group
                            invGroup = mInvites.get(position);
                            invGroupID = invGroup.getGroupID();
                            invGroupName = invGroup.getGroupName();

                            // Add group data to hash
                            hashGroup = new HashMap<>();
                            hashGroup.put("Group_ID", invGroupID);
                            hashGroup.put("Group_Name", invGroupName);

                            // Add user data to hash
                            hashUser = new HashMap<>();
                            hashUser.put("Display_Name", mUser.getDisplayName());
                            hashUser.put("Firebase_ID", mUser.getUid());
                            hashUser.put("Permission", "User");
                            hashUser.put("Username", mUser.getEmail());
                            //TODO: add real-time status addition (would need the oncallback nest)

                            // Updates the list of groups you're a part of
                            db.collection("Users").document(mUser.getUid())
                                    .collection("Groups").document(invGroupID)
                                    .set(hashGroup)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Added groupDoc to user database");
                                        }
                                    });

                            // Adds your user document to the group
                            db.collection("Groups").document(invGroupID)
                                    .collection("Users").document(mUser.getUid())
                                    .set(hashUser)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Added userDoc to group database");
                                        }
                                    });

                            // Delete the invitation
                            deleteInvitation(invGroupID, position);
                        }

                        @Override
                        public void onDeclineClick(int position) {
                            // Deletes Invitation
                            invGroup = mInvites.get(position);
                            invGroupID = invGroup.getGroupID();
                            deleteInvitation(invGroupID, position);
                        }
                    });

                } else {
                    inviteNotification.setHeight(0);
                    if(line.getParent() != null)
                        ((ViewGroup) line.getParent()).removeView(line);
                }

            }
        });

        // Read Database information about User's groups
        // 1: Acquire a List of Group Names
        readGroupNames(new listCallback() {
            @Override
            public void onCallback(ArrayList<String> groupNames, ArrayList<String> groupIDs, ArrayList<String> X) {
                Log.d(TAG, "=DEBUG= Callback Groups: " + groupNames.toString());
                mGroups.clear();
                // 2: Acquire a List of User Names (per group)
                for (int i = 0; i < groupNames.size(); i++) {
                    // Clears users & status lists (Different for each group)
                    final String groupName = groupNames.get(i);
                    final String groupID = groupIDs.get(i);
                    readUserNames(new listCallback() {
                        @Override
                        public void onCallback(ArrayList<String> uNames, ArrayList<String> uActivities,
                                               ArrayList<String> uStats) {
                            Log.d(TAG, "=DEBUG= Callback from User Names");

                            // Add to structure
                            Log.d(TAG, "=DEBUG= Group["+groupName+"]: has usernames["+uNames+"] & statuses["+uStats+"] & activities["+uActivities+"].");

                            mGroups.add(new GroupStructure(groupName, groupID, uNames, uStats, uActivities));
                            // this line increases performance & doesn't change size on num of items
                            mRVGroup.setHasFixedSize(true);
                            mAdapterGroup = new GroupAdapter(mGroups);

                            mRVGroup.setLayoutManager(mLMGroup);
                            mRVGroup.setAdapter(mAdapterGroup);

                            mAdapterGroup.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    // implementation for fragment method
                                    GroupStructure group = mGroups.get(position);
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("Group", group);

                                    GroupFragment frag = new GroupFragment();

                                    frag.setArguments(bundle);

                                    // WORKING CODE TO CHANGE FRAGMENTS WITH BUNDLE
                                    getFragmentManager().beginTransaction()
                                            .replace(((ViewGroup)getView().getParent()).getId(), frag)
                                            .addToBackStack(null).commit();
                                }
                            });
                        }
                    }, groupID);
                }
            }
        });
        Log.d(TAG, "=DEBUG= Returning root & drawing content");

        return root;
    }

    private void readInvites(final inviteCallback callback) {
        db.collection("Users").document(mUser.getUid())
                .collection("Invites")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot inviteTask,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        String groupName, groupID, hostName;
                        mInvites.clear();
                        for (QueryDocumentSnapshot doc_invite : inviteTask) {
                            groupName = doc_invite.getString("Group_Name");
                            groupID = doc_invite.getString("Group_ID");
                            hostName = doc_invite.getString("Host_Name");

                            mInvites.add(new InviteStructure(groupName, groupID, hostName));
                            Log.d(TAG, "=DEBUG= Retrieved " + groupName + " & adding to list");
                        }
                        callback.onCallback(mInvites);
                    }
                });
    }


    private void readGroupNames(final listCallback callback) {
        db.collection("Users")
                .document(mUser.getUid())
                .collection("Groups")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot groupTask,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        mGroups.clear();
                        ArrayList<String> gName = new ArrayList<>();
                        ArrayList<String> gID = new ArrayList<>();
                        for (QueryDocumentSnapshot doc_group : groupTask) { // Each Group a user is connected to
                            // we now have the group's name & ID
                            gName.add(doc_group.getString("Group_Name"));
                            gID.add(doc_group.getString("Group_ID"));
                        }
                        callback.onCallback(gName, gID, null);
                    }
                });
    }

    private void readUserNames(final listCallback callback, String ID) {
        Log.d(TAG, "=DEBUG= \tPerforming lookup on " + ID);
        db.collection("Groups")
                .document(ID)
                .collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> userTask) {
                        ArrayList<String> usernames = new ArrayList<>();
                        ArrayList<String> activities = new ArrayList<>();
                        ArrayList<String> statuses = new ArrayList<>();
                        if (userTask.isSuccessful()) {
                            String user, activity, status;
                            for (QueryDocumentSnapshot doc_user : userTask.getResult()) {
                                // we now have each of the users' names & status
                                user = doc_user.getString("Display_Name");
                                activity = doc_user.getString("Activity");
                                status = doc_user.getString("Status");

                                // Add to respective lists
                                usernames.add(user);
                                if (status != null) {
                                    statuses.add(status);
                                } else {
                                    statuses.add("");
                                }

                                if (activity != null) {
                                    activities.add(activity);
                                } else {
                                    activities.add("");
                                }

//                                Log.d(TAG, "=DEBUG= \t\tFound user [" + user + "] : " + status);
                            }
//                            Log.d(TAG, "=DEBUG= \tSuccess: Retrieved users.");
                        } else {
                            Log.d(TAG, "=DEBUG= \tError retrieving user docs");
                        }
                        callback.onCallback(usernames, activities, statuses);
                    }
                });
    }

    private void deleteInvitation(String ID, int pos) {
        // removes the invitation document
        db.collection("Users").document(mUser.getUid())
                .collection("Invites").document(ID).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Invitation deleted from db");
                    }
                });
        // removes from the view
        mInvites.remove(pos);
        mAdapterInv.notifyItemRemoved(pos);
    }

    public interface inviteCallback {
        void onCallback(ArrayList<InviteStructure> data);
    }

    public interface listCallback {
        void onCallback(ArrayList<String> data_X, ArrayList<String> data_Y, ArrayList<String> data_Z);
    }



}