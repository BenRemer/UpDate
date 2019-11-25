package com.gatech.update.Controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.github.omadahealth.lollipin.lib.PinActivity;
import com.github.omadahealth.lollipin.lib.PinCompatActivity;
import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gatech.update.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Boolean hasFingerprint;
//    private LockManager lockManager = LockManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, CustomPinActivity.class);
        lockManager.getAppLock().setOnlyBackgroundTimeout(true);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        Button launchAuthentication = findViewById(R.id.launchAuthentication);

        // Button listeners
        signInButton.setOnClickListener(this);
        launchAuthentication.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Set signin for this option
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Get auth instance
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Log.d(TAG, "------------------ googleSignInSuccess ------------------");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                // Start menu activity once the user has been logged in
//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
            } else {
                Log.d(TAG, "------------------ googleSignInFailure ------------------");
            }
        }
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_LONG).show();
//                            Intent intent = new Intent(getApplicationContext(), DrawerActivity.class);
//                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void googleSignIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void googleSignOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
//                        updateUI(null);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
//        SharedPreferences prefs = getApplication().getSharedPreferences("Prefs", 0);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Intent intent = new Intent(this, DrawerActivity.class);
            startActivity(intent);
//            Toast.makeText(getApplicationContext(), "Welcome Back", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Logged in");
        } else {
            Toast.makeText(getApplicationContext(), "Not logged In", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Not logged in");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Intent intent = new Intent(this, DrawerActivity.class);
            startActivity(intent);
            Log.d(TAG, "Logged in");
        } else {
            Toast.makeText(getApplicationContext(), "Not logged In", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Not logged in");
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            googleSignIn();
//        } else if (i == R.id.disconnectButton) {
//            revokeAccess();
        } else if(i == R.id.launchAuthentication){
            Intent intent = new Intent(this, CustomPinActivity.class);
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
            startActivityForResult(intent, 11);
        }
    }
}
