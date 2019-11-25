package com.gatech.update.Controller;

import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DeleteActivity extends AppCompatActivity {

    private Button button_yes, button_no;

    private String groupID;
    private ArrayList<String> userIDs;

    private FirebaseFirestore db;

    private String TAG = "DeleteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_delete);

        // Retrieves passed ID
        groupID = getIntent().getExtras().getString("ID");

        // Set up buttons
        button_no = findViewById(R.id.button_no);
        button_yes = findViewById(R.id.button_yes);

        // Set display metrics to determine area of window
        DisplayMetrics dispM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispM);
        int width = dispM.widthPixels;

        // set desired width, height -> can use percentage
        getWindow().setLayout((int)(width * 0.9), 600);

        // If user clicks no, back out of activity - no change
        button_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Method to handle when user confirms the deletion of the group
    public void confirmDelete(View v) {
        db = FirebaseFirestore.getInstance();
        // First, obtain all user IDs in group
        readUserIDs(new listCallback() {
            @Override
            public void onCallback(ArrayList<String> data) {
                Log.d(TAG, "=DEBUG= Callback Groups: " + userIDs.toString());

                // Delete primary document of group
                db.collection("Groups").document(groupID).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Group successfully deleted");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception err) {
                                Log.w(TAG, "Error deleting document", err);
                            }
                        });

                // Delete each instance of group per user
                for (final String mID: userIDs) {
                    db.collection("Users").document(mID)
                            .collection("Groups").document(groupID).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Group for user["+mID+"] removed");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting group for user["+mID+"]");
                                }
                            });
                }
                // finish the activity - database queries will complete asynchronously
                finish();
                Intent mIntent = new Intent(getApplication(), DrawerActivity.class);
                startActivity(mIntent);
            }
        });
    }

    private void readUserIDs(final listCallback callback) {
        Log.d(TAG, "=DEBUG= \tPerforming lookup on " + groupID);
        db.collection("Groups")
                .document(groupID)
                .collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> userTask) {
                        if (userTask.isSuccessful()) {
                            userIDs = new ArrayList<>();
                            for (QueryDocumentSnapshot doc_user : userTask.getResult()) {
                                // Add each id to list
                                userIDs.add(doc_user.getString("Firebase_ID"));
                            }
                            Log.d(TAG, "=DEBUG= \tSuccess: Retrieved users.");
                        } else {
                            Log.d(TAG, "=DEBUG= \tError retrieving user docs");
                        }
                        callback.onCallback(userIDs);
                    }
                });
    }

    public interface listCallback {
        void onCallback(ArrayList<String> data);
    }
}
