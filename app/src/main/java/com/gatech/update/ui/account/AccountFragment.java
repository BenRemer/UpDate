package com.gatech.update.ui.account;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.gatech.update.Controller.CustomPinActivity;
import com.gatech.update.Controller.DrawerActivity;
import com.gatech.update.Controller.LocationService;
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

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private DocumentReference userDoc;
    private static final String TAG = "AccountFragment";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Boolean hasFingerprint = false;
    private final long TIME_IMMEDIATE = 60;
    private final long TIME_ONE = 6000;
    private final long TIME_TWO = 12000;
    private final long TIME_FIVE = 30000;
    private final long TIME_TEN = 60000;
    private boolean background;

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
        final Switch pin_switch = root.findViewById(R.id.pin_switch);
        final Spinner timeout_spinner = root.findViewById(R.id.timeout_spinner);
        final Switch location_switch = root.findViewById(R.id.location_switch);
        final Switch background_switch = root.findViewById(R.id.background_switch);
        Button update = root.findViewById(R.id.update_button);
        Button logout = root.findViewById(R.id.logout_button);

        // Set title
        ((DrawerActivity) getActivity()).setActionBarTitle("Account Information");

        // For pin
        final LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.getAppLock().enable();

        final SharedPreferences prefs = getActivity().getSharedPreferences("Prefs", 0);
        SharedPreferences.Editor ed;
        if(!prefs.contains("background")){
            ed = prefs.edit();
            ed.putBoolean("background", true);
            ed.commit();
        }

        // Creating spinner elements
        List<String> times = new ArrayList<>();
        times.add("Immediately");
        times.add("1 Minute");
        times.add("2 Minutes");
        times.add("5 Minutes");
        times.add("10 Minutes");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, times);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeout_spinner.setAdapter(dataAdapter);


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
                String timeout = timeout_spinner.getSelectedItem().toString();
                switch (timeout){
                    case "Immediately":
                        lockManager.getAppLock().setTimeout(TIME_IMMEDIATE);
                        break;
                    case "1 Minute":
                        lockManager.getAppLock().setTimeout(TIME_ONE);
                        break;
                    case "2 Minutes":
                        lockManager.getAppLock().setTimeout(TIME_TWO);
                        break;
                    case "5 Minute":
                        lockManager.getAppLock().setTimeout(TIME_FIVE);
                        break;
                    case "10 Minute":
                        lockManager.getAppLock().setTimeout(TIME_TEN);
                        break;
                }
                Toast.makeText(getContext(), "Account Updated", Toast.LENGTH_LONG).show();
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
                    lockManager.getAppLock().setTimeout(TIME_IMMEDIATE);
                    timeout_spinner.setSelection(0);
                } else { // If off ask for pin before turning off
//                    lockManager.getAppLock().setPasscode(null);
                    lockManager.disableAppLock();
                    Intent intent = new Intent(getContext(), CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK);
                    startActivity(intent);
                }
            }
        });

        // Location services switch
        background_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(background_switch.isChecked()){
                    if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(getContext(), "Must enable location permissions", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
                    } else {
                        SharedPreferences.Editor ed;
                        ed = prefs.edit();
                        ed.putBoolean("background", true);
                        ed.commit();
                        Intent background_service = new Intent(getContext(), LocationService.class);
                        getContext().startService(background_service);
                    }
                } else {
                    SharedPreferences.Editor ed;
                    ed = prefs.edit();
                    ed.putBoolean("background", false);
                    ed.commit();
                    Intent background_service = new Intent(getContext(), LocationService.class);
                    getContext().stopService(background_service);
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
        Switch location_switch = getView().findViewById(R.id.location_switch);
        Switch background_switch = getView().findViewById(R.id.background_switch);
        Spinner timeout_input = getView().findViewById(R.id.timeout_spinner);
        TextView timeout_text = getView().findViewById(R.id.text_timeout);
        LinearLayout timeout_layout = getView().findViewById(R.id.timeout_layout);
        final SharedPreferences prefs = getActivity().getSharedPreferences("Prefs", 0);
        SharedPreferences.Editor ed;
        if(!prefs.contains("background")){
            ed = prefs.edit();
            ed.putBoolean("background", true);
            ed.commit();
        }
        if(prefs.getBoolean("background", false)){
            background_switch.setChecked(true);
        }else{
            background_switch.setChecked(false);
        }
        if(LockManager.getInstance().getAppLock().isPasscodeSet()) {
            pin_switch.setChecked(true);
//            timeout_text.setVisibility(View.VISIBLE);
//            timeout_input.setVisibility(View.VISIBLE);
            timeout_layout.setVisibility(View.VISIBLE);
            long time = lockManager.getAppLock().getTimeout();
            if(time == TIME_IMMEDIATE)
                timeout_input.setSelection(0);
            else if(time == TIME_ONE)
                timeout_input.setSelection(1);
            else if(time == TIME_TWO)
                timeout_input.setSelection(2);
            else if(time == TIME_FIVE)
                timeout_input.setSelection(3);
            else
                timeout_input.setSelection(4);
//            timeout_input.setText(time.toString());
//            timeout_input.setText((int)lockManager.getAppLock().getTimeout());
        } else {
//            timeout_text.setVisibility(View.GONE);
//            timeout_input.setVisibility(View.GONE);
            timeout_layout.setVisibility(View.GONE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location_switch.setChecked(true);
        } else {
            location_switch.setChecked(false);
        }

    }
}