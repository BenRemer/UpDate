package com.gatech.update.Controller;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.gatech.update.R;
import com.github.omadahealth.lollipin.lib.PinCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewStatusActivity extends PinCompatActivity implements View.OnClickListener {
    // Initialize inputs for updating status
    private TextInputEditText input_status;
    private FirebaseUser mUser;
    private String userID;
    private RadioButton disturb, quiet, studying, avaliable, working, social;

    private ArrayList<String> groupIDs;
    private String activity;

    private static final String TAG = "NewStatusActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // Obtain information about current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mUser.getUid();

        groupIDs = new ArrayList<>();

        // Find radio Buttons
        disturb = findViewById(R.id.radio_disturb);
        quiet = findViewById(R.id.radio_quiet);
        studying = findViewById(R.id.radio_studying);
        avaliable = findViewById(R.id.radio_avaliable);
        working = findViewById(R.id.radio_working);
        social = findViewById(R.id.radio_social);

        // Set Click Listener
        disturb.setOnClickListener(this);
        quiet.setOnClickListener(this);
        studying.setOnClickListener(this);
        avaliable.setOnClickListener(this);
        working.setOnClickListener(this);
        social.setOnClickListener(this);

        setRadio();

        // Set display metrics to determine area of window
        DisplayMetrics dispM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispM);
        int width = dispM.widthPixels;
        int height = dispM.heightPixels;

        // set desired width, height -> can use percentage
        getWindow().setLayout((int)(width * 0.9), (int)(height * 0.6));

        // Create a listener for input box
        input_status = findViewById(R.id.input_newStatus);

        // Place old status in text window
        readStatus(new stringCallback() {
            @Override
            public void onCallback(String status) {
                input_status.setText(status);
            }
        });

    }

    /** updateStatus() reads the text input by the user and updates the relevant FireStore entries
     *      (for the User file & each group the User is acquainted with)
     *
     * @param v - the current view
     */
    public void updateStatus(View v) {
        // TODO: Either don't allow now input or auto-fill old information
        //  - can add another callback to perform this (may have to nest within previous callback)
        // Set their activity
        update(activity);
        // Add status to the map structure
        final Map<String, Object> status = new HashMap<>();
        String status_text = input_status.getText().toString();
        status.put("Status", status_text);
        status.put("Activity", activity);

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

    private void readStatus(final stringCallback callback) {
        db.collection("Users").document(userID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String status;
                        if (task.isSuccessful()) {
                            status = task.getResult().getString("Status");
                            if (status == null) {
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
    public interface listCallback {
        void onCallback(ArrayList<String> data);
    }

    public interface stringCallback {
        void onCallback(String data);
    }

    @Override
    public void onClick(View v) {
        clearRadio();
        int i = v.getId();
        switch(i){
            case R.id.radio_disturb:
                disturb.setChecked(true);
                activity = "Disturb";
                break;

            case R.id.radio_studying:
                studying.setChecked(true);
                activity = "Studying";
                break;

            case R.id.radio_avaliable:
                avaliable.setChecked(true);
                activity = "Available";
                break;

            case R.id.radio_quiet:
                quiet.setChecked(true);
                activity = "Quiet";
                break;

            case R.id.radio_social:
                social.setChecked(true);
                activity ="Social";
                break;

            case R.id.radio_working:
                working.setChecked(true);
                activity ="Working";
                break;
        }
    }

    private void setRadio(){
        db.collection("Users")
                .document(mUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                String radio = document.getString("Activity");
                                if(radio != null){
                                    if(radio.equals("Disturb"))
                                        disturb.setChecked(true);
                                    else if(radio.equals("Studying"))
                                        studying.setChecked(true);
                                    else if(radio.equals("Available"))
                                        avaliable.setChecked(true);
                                    else if(radio.equals("Quiet"))
                                        quiet.setChecked(true);
                                    else if(radio.equals("Social"))
                                        social.setChecked(true);
                                    else if(radio.equals("Working"))
                                        working.setChecked(true);
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    public void clearRadio(){
        social.setChecked(false);
        quiet.setChecked(false);
        studying.setChecked(false);
        avaliable.setChecked(false);
        working.setChecked(false);
        disturb.setChecked(false);
    }

    private void update(String activity){
        Map<String, Object> Activity = new HashMap<>();
        Activity.put("Activity", activity);
        db.collection("Users")
                .document(mUser.getUid())
                .set(Activity, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    //                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                        finish();
//                    }
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot written for group");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document to Groups", e);
                    }
                });
    }
}
