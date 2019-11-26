package com.gatech.update.Controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gatech.update.R;
import com.github.omadahealth.lollipin.lib.PinCompatActivity;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class DrawerActivity extends PinCompatActivity { //AppCompatActivity

    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
//    private FirebaseDatabase database;
//    private DatabaseReference myRef;
    private FirebaseUser mUser;

    // for image permissions
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    Uri imgURI;
    private ImageView pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Starts the activity to create a new status (popup window similar to create group)
                startActivity(new Intent(DrawerActivity.this, NewStatusActivity.class));
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Grabs the header inside of nav view - edit header relative to user
        View navHead = navigationView.getHeaderView(0);

        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.getAppLock().setOnlyBackgroundTimeout(true);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_group, R.id.nav_account, R.id.nav_map,
                R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mAuth = FirebaseAuth.getInstance();
//        database = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();

//        myRef = database.getReference("where");
//        myRef.child(Objects.requireNonNull(mAuth.getUid())).setValue("Test");

        // Set name & email for user
        final TextView name = navHead.findViewById(R.id.text_name_d);
        final TextView email = navHead.findViewById(R.id.text_email_d);
        pic = navHead.findViewById(R.id.sidebar_pic);

        imgURI = mUser.getPhotoUrl();

        // See if user permissions
        int permissions = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissions != PackageManager.PERMISSION_GRANTED) {
            // Create the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // We have permissions. read image
            try {
                pic.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), imgURI));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        name.setText(mUser.getDisplayName());
        email.setText(mUser.getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                try {
                    pic.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), imgURI));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed(){ // When back is pressed, reload to main screen to stop any duplications
////        this.finish();
//        Log.d("Back", "back");
//        this.finish();
        Intent intent = new Intent(this, DrawerActivity.class);
        startActivity(intent);
//        super.onBackPressed();
    }
}
