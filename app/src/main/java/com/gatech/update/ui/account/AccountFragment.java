package com.gatech.update.ui.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gatech.update.Controller.CustomPinActivity;
import com.gatech.update.Controller.LoginActivity;
import com.gatech.update.R;
import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AccountFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private DocumentReference userDoc;

    private static final String TAG = "AccountFragment";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Boolean hasFingerprint = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        accountViewModel =
                ViewModelProviders.of(this).get(AccountViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_account, container, false);
//        final TextView textView = root.findViewById(R.id.text_gallery);
//        accountViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final TextView status = root.findViewById(R.id.text_status);
        final TextView username = root.findViewById(R.id.name_edittext);
        final TextView email = root.findViewById(R.id.email_edittext);
//        final Switch fingerprint_switch = root.findViewById(R.id.fingerprint_switch);
        final Switch pin_switch = root.findViewById(R.id.pin_switch);
//        final EditText timeout_input = root.findViewById(R.id.timeout_input);
//        final TextView timeout_text = root.findViewById(R.id.timeout_text);
        Button update = root.findViewById(R.id.update_button);
        Button logout = root.findViewById(R.id.logout_button);

        // For pin
        final LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.getAppLock().enable();


        // Get username and email and update them
        username.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // update info
                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username.getText().toString()).build();
                user.updateProfile(request)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Log.d(TAG, "User display_name added");
                                }
                            }
                        }
                );
                user.updateEmail(email.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User email address updated.");
                                }
                            }
                        });
//                long timeout = Long.parseLong(timeout_input.getText().toString());
//                timeout = timeout * 10000 * 60;
//                if(timeout == 0){
//                    Toast.makeText(getContext(), "Must be at least 1", Toast.LENGTH_LONG).show();
//                    int time =(int) lockManager.getAppLock().getTimeout()/1000/60;
//                    timeout_input.setText(Integer.toString(time));
//                    timeout_input.setSelection(timeout_input.getText().length());
//                } else {
//                    lockManager.getAppLock().enable();
////                    lockManager.getAppLock().setOnlyBackgroundTimeout(true);
//                    lockManager.getAppLock().setTimeout(timeout);
//                }
            }
        });

        // Check what state pin_switch is switched to
        pin_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
                if(pin_switch.isChecked()){ // If turned on set lock pi
                    lockManager.enableAppLock(getContext(), CustomPinActivity.class);
                    lockManager.getAppLock().setShouldShowForgot(false);
                    Intent intent = new Intent(getContext(), CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                    startActivity(intent);
                    lockManager.getAppLock().setOnlyBackgroundTimeout(true);
                    lockManager.getAppLock().setTimeout(60);
                } else { // If off ask for pin before turning off
//                    lockManager.getAppLock().setPasscode(null);
                    lockManager.disableAppLock();
                    Intent intent = new Intent(getContext(), CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK);
                    startActivity(intent);
                }
            }
        });

        // Logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage("Are you sure you want to logout?\nIf you have a passcode it will be removed.");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) { // Sign out, turn off pin
                                FirebaseAuth.getInstance().signOut();
                                LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
                                lockManager.getAppLock().setPasscode(null);
                                lockManager.disableAppLock();
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK);
                                startActivity(intent);
//                        mGoogleSignInClient.signOut();
//                        dialog.cancel();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        // Obtain user's status info and display it
        final String userID = user.getUid();
//        userDoc = db.collection("Users")
//                .document(userID);
        db.collection("Users")
                .document(userID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                status.setText(document.getString("Status"));
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

//        name.setText(user.getDisplayName());

        return root;
    }

    @Override
    public void onResume(){ // When resumed, switch pins to correct locations
        super.onResume();
        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        Switch pin_switch = getView().findViewById(R.id.pin_switch);
//        EditText timeout_input = getView().findViewById(R.id.timeout_input);
//        TextView timeout_text = getView().findViewById(R.id.timeout_text);
        if(LockManager.getInstance().getAppLock().isPasscodeSet()) {
            pin_switch.setChecked(true);
//            timeout_text.setVisibility(View.VISIBLE);
//            timeout_input.setVisibility(View.VISIBLE);
//            long time = lockManager.getAppLock().getTimeout();
//            Long time = lockManager.getAppLock().getTimeout();
//            timeout_input.setText(time.toString());
//            timeout_input.setText((int)lockManager.getAppLock().getTimeout());
        } else {
//            timeout_text.setVisibility(View.GONE);
//            timeout_input.setVisibility(View.GONE);
        }
    }
}