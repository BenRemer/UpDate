package com.gatech.update.Controller;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.gatech.update.R;
import com.github.omadahealth.lollipin.lib.PinActivity;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateGroupActivity extends PinActivity {
    private TextInputLayout groupName;
    private FirebaseUser mUser;
    private String mStatus;
    private Map<String, Object> group, user;

    private static final String TAG = "CreateGroupActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Set display metrics to determine area of window
        DisplayMetrics dispM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispM);
        int width = dispM.widthPixels;

        // set desired width, height -> can use percentage
        getWindow().setLayout((int)(width * 0.6), 500);

        // Create a listener for input box
        groupName = findViewById(R.id.input_groupName);
    }

    // Sets guidelines for Group Names
    // currently checks if nothing is entered
    private boolean validateGroupName() {
        String groupInput = groupName.getEditText().getText().toString().trim();
        if (groupInput.isEmpty()) {
            groupName.setError("Must Enter a Group Name");
            return false;
        } else {
            groupName.setError(null);
            return true;
        }
    }

    // The confirmation for creating a group
    public void createGroup(View v) {
        Log.d(TAG, "CREATE REQUEST");
        if (!validateGroupName())
            return;
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        // TODO: Create group and add user to database
        //       need current user email, set privilege to OWNER
        // ...
        group = new HashMap<>();
        user = new HashMap<>();

        // used to create group hash
        final String userID = mUser.getUid();
        String name = groupName.getEditText().getText().toString();
        final int groupHash = userID.hashCode() * name.hashCode();

        // Add the group name to the document
        group.put("Group_Name", name);
        group.put("Group_ID", "" + groupHash);

        // Add the owners information
        user.put("Username", Objects.requireNonNull(mUser.getEmail()));
        user.put("Display_Name", Objects.requireNonNull(mUser.getDisplayName()));
        user.put("Firebase_ID", userID);
        user.put("Permission", "Owner");

        // Grab user's status
        readStatus(new stringCallback() {
            /** onCallback() is performed after completion of readStatus. This allows us to wait
             *      for the asynchronous read before we write the critical data back into the db.
             *
             * @param status - the returned status of the user. Empty ("") if read fails or not set.
             */
            @Override
            public void onCallback(String status) {
                Log.d(TAG, "=DEBUG= Callback status: " + status);

                // create document and put group in it
                db.collection("Groups")
                        .document(Integer.toString(groupHash))
                        .set(group)
                        // --Code via Android Studio Helper menu--
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            //                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                        finish();
//                    }
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot written for group");
                                // create user doc under Users under Group
                                db.collection("Groups").document(Integer.toString(groupHash)).collection("Users")
                                        .document(userID)
                                        .set(user);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document to Groups", e);
                                finish();
                            }
                        });

                // now add the group ID to the associated user collection
                db.collection("Users")
                        .document(userID)
                        .update(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {

                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot written for user");
                                db.collection("Users")
                                        .document(userID)
                                        .collection("Groups")
                                        .document(Integer.toString(groupHash))
                                        .set(group);
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
            }
        }, userID);

    }

    /** readStatus() is called with a user's ID and performs a FireStore read for the user's status
     *      it adds the status to the user structure (if previously set), or adds an empty status
     *
     * @param callback - the callback to call once query complete
     * @param uID - the user's ID (unchanging) for database queries
     */
    private void readStatus(final stringCallback callback, final String uID) {
        db.collection("Users")
        .document(uID)
        .get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String status;
                if (task.isSuccessful()) {
                    mStatus = task.getResult().getString("Status");
                    if (mStatus != null) {
                        status = mStatus;
                    } else {
                        status = "";
                    }
                } else {
                    Log.d(TAG, "=DEBUG= unable to collect status of user");
                    status = "";
                }

                callback.onCallback(status);
            }
        });
    }

    /** Callback's interface - holds the onCallback method to be performed
     */
    public interface stringCallback {
        void onCallback(String data);
    }
}
