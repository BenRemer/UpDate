package com.gatech.update.Controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateGroupActivity extends Activity {
    private TextInputLayout groupName;
    private FirebaseUser mUser;

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
        int height = dispM.heightPixels;

        // set desired width, height -> can use percentage
        getWindow().setLayout((int)(width * 0.6), 500);

        // Create a listener for input box
        groupName = findViewById(R.id.input_groupName);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
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
        String name = groupName.getEditText().getText().toString();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        // TODO: Create group and add user to database
        //       need current user email, set privilege to OWNER
        // ...
        final Map<String, Object> group = new HashMap<>();
        final Map<String, Object> user = new HashMap<>();

        // Add the group name to the document
        group.put("Group_Name", name);
//        group.put("User", Objects.requireNonNull(mUser.getEmail()));
//        group.put("Permission", 2);

        // Add the owners information
        user.put("Username", Objects.requireNonNull(mUser.getEmail()));
        user.put("Display_Name", Objects.requireNonNull(mUser.getDisplayName()));
        user.put("Firebase_ID", Objects.requireNonNull(mUser.getUid()));
        user.put("Permission", "Owner");
        // Attempt 2: Only Place group name inside of group collections
//        group.put("Group Name", groupName.toString());

        // Add user's info to group
//        final DocumentReference ref = db.collection("Groups").document(name);
//        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()){
//                    DocumentSnapshot snap = task.getResult();
//                    if (snap.exists()){
//                        Log.d(TAG, "Doc exists");
//
//                    } else {
//                        Log.d(TAG, "Doc doesn't exist yet");
//                        ref.set(group);
//                    }
//                } else {
//                    Log.d(TAG, "Failed with: ", task.getException());
//                }
//            }
//        });
        // create document and put group in it
        db.collection("Groups")
//                .add(groupName.toString())
                .document(mUser.getUid())
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
                        Log.d(TAG, "DocumentSnapshot written");
                        // create user doc under Users under Group
                        db.collection("Groups").document(mUser.getUid()).collection("Users")
                                .document(mUser.getUid())
                                .set(user);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        finish();
                    }
                });


    }
}
