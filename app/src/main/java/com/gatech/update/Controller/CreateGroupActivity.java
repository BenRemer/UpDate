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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateGroupActivity extends Activity {
    private TextInputLayout groupName;
    private FirebaseUser mUser;
    private String mStatus;

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
        final Map<String, Object> group = new HashMap<>();
        final Map<String, Object> user = new HashMap<>();

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
        // Not really necessary
//        db.collection("Users")
//                .document(userID)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            mStatus = Objects.requireNonNull(task.getResult()).getString("Status");
//                            if (mStatus != null && !mStatus.isEmpty()) {
//                                user.put("Status", mStatus);
//                            }
//                        } else {
//                            Log.d(TAG, "=DEBUG= unable to collect status of user");
//                        }
//                    }
//                });

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
                .set(user)
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
}
