package com.gatech.update.Controller;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.gatech.update.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> group = new HashMap<>();

        // Attempt 1: Put Email & Permission of 2 (Group owner)
//        group.put("User", Objects.requireNonNull(mUser.getEmail()));
//        group.put("Permission", 2);

        // Attempt 2: Only Place group name inside of group collections
        group.put("Group Name", groupName.toString());

        // Add user's info to group
        db.collection("Groups")
                .add(group)
                // --Code via Android Studio Helper menu--
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
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
