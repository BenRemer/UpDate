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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewStatusActivity extends Activity {
    // Initialize inputs for updating status
    private TextInputEditText input_status;
    private FirebaseUser mUser;
    private String userID;

    private static final String TAG = "NewStatusActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // Set display metrics to determine area of window
        DisplayMetrics dispM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dispM);
        int width = dispM.widthPixels;
        int height = dispM.heightPixels;

        // set desired width, height -> can use percentage
        getWindow().setLayout((int)(width * 0.9), (int)(height * 0.5));

        // Create a listener for input box
        input_status = findViewById(R.id.input_newStatus);
    }

    // OnCLick for update button
    public void updateStatus(View v) {
        // TODO: Either don't allow now input or auto-fill old information
        //      why? so user doesn't accidentally erase old status by entering nothing

        // Obtain information about current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mUser.getUid();

        // Add status to the map structure
        final Map<String, Object> status = new HashMap<>();
        String status_text = input_status.getText().toString();
        status.put("Status", status_text);

        // Add the user's new status to the database
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

    }
}
