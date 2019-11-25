package com.gatech.update.Controller;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gatech.update.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InviteUserActivity extends AppCompatActivity {

    private Button buttonInvite, buttonCancel;
    private TextInputLayout input_email;
    private FirebaseFirestore db;
    private String groupID, groupName;
    private Map<String, Object> invitation;

    // For debug purposes
    private String TAG = "InviteUserActivity";

    // Local user vars
    private FirebaseUser mUser;
    private String mID, mName, mEmail;

    // Searched user vars
    private String targID, targName, targEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_invite_user);

        // link buttons
        buttonInvite = findViewById(R.id.button_invite);
        buttonCancel = findViewById(R.id.button_cancel);
        // Link text to edit
        input_email = findViewById(R.id.input_email);

        // Retrieves passed ID & Group Name
        groupID = getIntent().getExtras().getString("ID");
        groupName = getIntent().getExtras().getString("Name");

        // Set display metrics to determine area of window
        DisplayMetrics dispM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispM);
        int width = dispM.widthPixels;

        // set desired width, height -> can use percentage
        getWindow().setLayout((int)(width * 0.9), 600);

        // If user clicks cancel, back out of activity - no change
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // If user clicks to invite user - adds group to user invitation
    public void InviteUser(View v) {
        // updates targEmail to input text
        targEmail = input_email.getEditText().getText().toString().trim();
        if (!validateUser(targEmail))
            return;
        db = FirebaseFirestore.getInstance();

        // Retrieves self information
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mID = mUser.getUid();
        mName = mUser.getDisplayName();
        mEmail = mUser.getEmail();

        // Perform search (based on button)
        searchForUser(new stringCallback() {
            @Override
            public void onCallback(String uID) {
                // Create a map to store in the DB
                invitation = new HashMap<>();
                invitation.put("Group_ID", groupID);
                invitation.put("Group_Name", groupName);
                invitation.put("Host_Name", mName);
                invitation.put("Host_Email", mEmail);

                // Creates a new document for invite:  User -> Invites -> groupID
                db.collection("Users").document(targID)
                        .collection("Invites").document(groupID).set(invitation)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            //                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                        finish();
//                    }
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot written for invitation");
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document to invitations", e);
                                finish();
                            }
                        });
            }
        });
    }

    // Sets guidelines for Group Names
    // currently checks if nothing is entered
    private boolean validateUser(String email) {
        // Set up parameters to check email validity
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher match = pattern.matcher(email);

        if (email.isEmpty()) {
            input_email.setError("Field cannot be empty");
            return false;
        } else if (email.equals(mEmail)) {
            input_email.setError("You are already a part of the group");
            return false;
        } else if (!match.matches()) {
            input_email.setError("Must enter valid email address");
            return false;
        } else {
            input_email.setError(null);
            return true;
        }
    }

    private void searchForUser(final stringCallback callback) {
        db.collection("Users").whereEqualTo("Username", targEmail).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot targDoc : task.getResult()) {
                                targID = targDoc.getString("Firebase_ID");
                                targName = targDoc.getString("Display_Name");
                                Log.d(TAG, "=DEBUG= Found user: " + targName + " with id: " + targID);
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                        callback.onCallback(targID);
                    }
                });
    }

    public interface stringCallback {
        void onCallback(String uID);
    }

}
