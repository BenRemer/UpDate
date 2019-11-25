package com.gatech.update.Controller;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.gatech.update.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewStatusActivity extends Activity {
    // Initialize inputs for updating status
    private TextInputEditText input_status;
    private FirebaseUser mUser;
    private String userID;

    private ArrayList<String> groupIDs;

    private static final String TAG = "NewStatusActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        groupIDs = new ArrayList<>();

        // Set display metrics to determine area of window
        DisplayMetrics dispM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispM);
        int width = dispM.widthPixels;
        int height = dispM.heightPixels;

        // set desired width, height -> can use percentage
        getWindow().setLayout((int)(width * 0.9), (int)(height * 0.4));

        // Create a listener for input box
        input_status = findViewById(R.id.input_newStatus);
    }

    /** updateStatus() reads the text input by the user and updates the relevant FireStore entries
     *      (for the User file & each group the User is acquainted with)
     *
     * @param v - the current view
     */
    public void updateStatus(View v) {
        // TODO: Either don't allow now input or auto-fill old information
        //  - can add another callback to perform this (may have to nest within previous callback)

        // Obtain information about current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mUser.getUid();

        // Add status to the map structure
        final Map<String, Object> status = new HashMap<>();
        String status_text = input_status.getText().toString();
        status.put("Status", status_text);

        // Add the user's new status to user's database
        db.collection("Users")
                .document(userID)
                .update(status)
                //.set(status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.w(TAG, "DocumentSnapshot written for user status");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document to Users", e);
                        finish();
                    }
                });

        // We want to retrieve the list of groups the user is a part of
        readGroupIDs(new listCallback() {
            /** onCallback() is performed after the completion of readGroupIDs. This allows us to
             *      wait for the asynchronous read before we write the critical data back to the db.
             *
             * @param groupIDs - the returned groupIDs for the user
             */
            @Override
            public void onCallback(ArrayList<String> groupIDs) {
                Log.d(TAG, "=DEBUG= Callback IDs: " + groupIDs.toString());

               // Now with this list, we can begin to update each status
                for (String ID: groupIDs) {
                    db.collection("Groups")
                            .document(ID)
                            .collection("Users")
                            .document(userID)
                            .update(status)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "=DEBUG= UNABLE TO UPDATE USER GROUPS");
                                }
                            });
                }
            }
        });


    }

    /** readGroupIDs() performs a FireStore read for all the groupIDs a user is part of.
     *      returns the groupID List or an empty list
     *
     * @param callback - the callback to call once query is complete
     */
    private void readGroupIDs(final listCallback callback) {
        db.collection("Users")
                .document(userID)
                .collection("Groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> groupTask) {
                        if (groupTask.isSuccessful()) {
                            String ID;
                            for (QueryDocumentSnapshot doc_group : groupTask.getResult()) { // Each Group a user is connected to
                                // we now have the group's ID
                                ID = doc_group.getString("Group_ID");
                                groupIDs.add(ID);
                            }
                        } else {
                            Log.d(TAG, "=DEBUG= Error retrieving group docs.");
                        }
                        callback.onCallback(groupIDs);
                    }
                });
    }

    /** Callback's interface - holds the onCallback method to be performed
     */
    public interface listCallback {
        void onCallback(ArrayList<String> data);
    }
}
