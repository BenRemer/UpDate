package com.gatech.update.Controller;

import android.content.Intent;
import android.os.Bundle;

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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        // Button listeners
        signInButton.setOnClickListener(this);

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
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                //Log.d(TAG, result.getStatus().getStatusMessage());
//                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
//        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(getApplicationContext(), DrawerActivity.class);
                            startActivity(intent);
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_LONG).show();
//                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressDialog();
                        // [END_EXCLUDE]
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
//        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            Intent intent = new Intent(this, DrawerActivity.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Already Logged In", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Logged in");
        } else {
            Toast.makeText(getApplicationContext(), "Not logged In", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Not logged in");
        }
//        updateUI(currentUser)
//        GoogleSignInAccount alreadyloggedAccount = GoogleSignIn.getLastSignedInAccount(this);
//        if (alreadyloggedAccount != null) {
////            Intent intent = new Intent(this, MainActivity.class);
////            startActivity(intent);
////            finish();
//            Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(this, DrawerActivity.class);
//            startActivity(intent);
////            onLoggedIn(alreadyloggedAccount);
//        } else {
//            Log.d(TAG, "Not logged in");
//        }

    }

//    private static String getToken(String filePath) {
//        StringBuilder contentBuilder = new StringBuilder();
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//
//            String sCurrentLine;
//            while ((sCurrentLine = br.readLine()) != null) {
//                contentBuilder.append(sCurrentLine).append("\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return contentBuilder.toString();
//    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            googleSignIn();
//        } else if (i == R.id.disconnectButton) {
//            revokeAccess();
        }
    }
}
