package com.gatech.update.ui.logout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gatech.update.Controller.CustomPinActivity;
import com.gatech.update.Controller.LoginActivity;
import com.gatech.update.R;
import com.gatech.update.ui.home.HomeFragment;
import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutFragment extends Fragment {

//    private GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build();
    private LogoutViewModel logoutViewModel;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

//    private GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        logoutViewModel =
                ViewModelProviders.of(this).get(LogoutViewModel.class);
        View root = inflater.inflate(R.layout.fragment_logout, container, false);

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setMessage("Are you sure you want to logout?\nIf you have a passcode it will be removed.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
//                        Intent intent = new Intent(getContext(), HomeFragment.class);
//                        startActivity(intent);
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
        return root;
    }
}