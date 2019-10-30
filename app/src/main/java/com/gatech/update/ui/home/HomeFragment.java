package com.gatech.update.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gatech.update.Controller.CreateGroupActivity;
import com.gatech.update.Controller.MainActivity;
import com.gatech.update.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ScrollView myGroups;
    private Button desiredGroup;
    private LinearLayout ll;
    private List<Type> myList = new ArrayList<>();
    private LinearLayout.LayoutParams params;

    private FirebaseUser mUser;

    private String TAG = "DisplayGroupInfo";

    int currGroupNum;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        currGroupNum = 1;

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
        }

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get list of groups
        myGroups = root.findViewById(R.id.groupInfo);
        ll = root.findViewById(R.id.ll);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        db.collection("Users")
                .document(mUser.getUid())
                .collection("Groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                // Dynamically adds buttons to the home screen of all groups user is
                                // a part of
                                desiredGroup = new Button(getContext());
                                desiredGroup.setText(doc.getData().get("Group_Name").toString());
                                ll.addView(desiredGroup, params);

                                /* TODO: Display Information of each group (Names of those in each
                                         group & their status -> 2+ ways to implement)
                                         1 - The "GroupMe" method (Buttons bring you to separate
                                             screen & display information there)
                                         2 - The "Facebook" method (all information on a scrolling
                                             homepage - no need for buttons, could change to text)
                                 */

                            }
                            Log.d(TAG, "Success: Retrieved: " + myList);
                        } else {
                            Log.d(TAG, "Error retrieving docs.");
                        }
                    }
                });

        // Should pull information based on groups of user
        // User should be able to start a new group from start
        final Button newGroupButton = root.findViewById(R.id.create_group_b);
        newGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Begins the new activity (Overlay create group pane)
                startActivity(new Intent(getActivity(), CreateGroupActivity.class));
            }
        });

        return root;
    }
}