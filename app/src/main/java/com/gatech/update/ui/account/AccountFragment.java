package com.gatech.update.ui.account;

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
        final TextView name = root.findViewById(R.id.text_name);
        final TextView status = root.findViewById(R.id.text_status);
        final TextView username = root.findViewById(R.id.name_edittext);
        final TextView email = root.findViewById(R.id.email_edittext);
        final Switch fingerprint_switch = root.findViewById(R.id.fingerprint_switch);
        final Switch pin_switch = root.findViewById(R.id.pin_switch);
        Button update = root.findViewById(R.id.update_button);

        // For saving fingerprint information
        SharedPreferences settings = getContext().getSharedPreferences("Prefs", 0);
        final SharedPreferences.Editor editor = settings.edit();

        // Get from the SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences("Prefs", 0);
        hasFingerprint = prefs.getBoolean("fingerprint", false);
        if(hasFingerprint){
            fingerprint_switch.setChecked(true);
        }

        if(LockManager.getInstance().getAppLock().isPasscodeSet()) {
            pin_switch.setChecked(true);
        }

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
            }
        });
        // For fingerprint
        Executor newExecutor = Executors.newSingleThreadExecutor();
        FragmentActivity activity = this.getActivity();
        final BiometricPrompt myBiometricPrompt = new BiometricPrompt(activity, newExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            //onAuthenticationError is called when a fatal error occurrs//
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    if(Looper.myLooper() == null)
                        Looper.prepare();
                    updateSwitch(false, root);
//                    fingerprint_switch.setChecked(false);
                } else {
                    //Print a message to Logcat//
                    Log.d(TAG, "An unrecoverable error occurred");
                    if(Looper.myLooper() == null)
                        Looper.prepare();
                    updateSwitch(false, root);
//                    fingerprint_switch.setChecked(false);
                }
            }
            //onAuthenticationSucceeded is called when a fingerprint is matched successfully//
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                //Print a message to Logcat//
                Log.d(TAG, "Fingerprint recognised successfully");
                if(Looper.myLooper() == null)
                    Looper.prepare();
                Toast.makeText(getContext(), "Fingerprint Added", Toast.LENGTH_LONG).show();
                updateSwitch(true, root);
                editor.putBoolean("fingerprint", true);
                editor.apply();
            }
            //onAuthenticationFailed is called when the fingerprint doesn't match//
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                //Print a message to Logcat//
                Log.d(TAG, "Fingerprint not recognised");
                if(Looper.myLooper() == null)
                    Looper.prepare();
//                fingerprint_switch.setChecked(false);
                updateSwitch(false, root);
            }
        });
        // Build fingerprint
        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint")
                .setDescription("Add Fingerprint to the app")
                .setNegativeButtonText("Cancel")
                .build();

        final FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(getContext());

        // check if can have fingerprint
        fingerprint_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fingerprint_switch.isChecked()) {
                    editor.putBoolean("fingerprint", false);
                    editor.apply();
                    Toast.makeText(getContext(), "Fingerprint Removed", Toast.LENGTH_LONG).show();
                } else {
                    if (!fingerprintManager.isHardwareDetected()) {
                        Toast.makeText(getContext(), "Your Device does not have a Fingerprint Sensor", Toast.LENGTH_LONG).show();
                        fingerprint_switch.setChecked(false);
                    } else {
                        // Checks whether fingerprint permission is set on manifest
                        if (!fingerprintManager.hasEnrolledFingerprints()) {
                            Toast.makeText(getContext(), "Register at least one fingerprint in Settings", Toast.LENGTH_LONG).show();
                            fingerprint_switch.setChecked(false);
                        } else {
                            myBiometricPrompt.authenticate(promptInfo);
                        }
                    }
                }
            }
        });

        pin_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pin_switch.isChecked()){
                    LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
                    lockManager.enableAppLock(getContext(), CustomPinActivity.class);
                    lockManager.getAppLock().setShouldShowForgot(false);
                    Intent intent = new Intent(getContext(), CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                    startActivity(intent);
                } else {
//                    Intent intent = new Intent(getContext(), CustomPinActivity.class);
//                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);
//                    startActivity(intent);
                }
            }
        });



        // Obtain user's status info
        final String userID = user.getUid();
        userDoc = db.collection("Users")
                .document(userID);
        userDoc.get()
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

        name.setText(user.getDisplayName());

        return root;
    }

    public void updateSwitch(final Boolean bool, final View root){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Switch fingerprint_switch = root.findViewById(R.id.fingerprint_switch);
                if(bool)
                    fingerprint_switch.setChecked(true);
                else
                    fingerprint_switch.setChecked(false);
            }
        });

    }
}