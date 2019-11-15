package com.gatech.update.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gatech.update.Controller.GroupActivity;
import com.gatech.update.Controller.GroupStructure;
import com.gatech.update.Controller.MapsActivity;
import com.gatech.update.R;
import com.gatech.update.ui.home.GroupAdapter;
import com.gatech.update.ui.home.HomeFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment {

    private MapViewModel mapViewModel;
    private GoogleMap googleMap;
    private MapView mMapView;
    private DocumentReference userDoc;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final String TAG = "MapsFragment";
    private ArrayList<GroupStructure> mGroups;
    // now a list for users in a certain group & their status...
    private ArrayList<String> mGroupNames;
    private ArrayList<String> mGroupIDs;
    private ArrayList<String> mUsers;
    private ArrayList<String> mUserIDs;
    private ArrayList<String> mStatus;
    private ArrayList<String> mLocations;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel =
                ViewModelProviders.of(this).get(MapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        mapViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
//        Intent intent = new Intent(getActivity(), MapsActivity.class);
//        startActivity(intent);

        getActivity().setTitle("Map");

        mMapView = (MapView) root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mGroups = new ArrayList<>();
        mGroupNames = new ArrayList<>();
        mGroupIDs = new ArrayList<>();
        mUsers = new ArrayList<>();
        mStatus = new ArrayList<>();
        mLocations = new ArrayList<>();
        mUserIDs = new ArrayList<>();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            mMapView.onResume();
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.7728837,-84.393816) , 14.0f) );
                }
            });
        } else {
            mMapView.onResume(); // needed to get the map to display immediately
            // https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial place tutorial
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.7728837,-84.393816) , 14.0f) );
                    // For showing a move to my location button
                    googleMap.setMyLocationEnabled(true);
                    GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange (Location location) {
                            LatLng loc = new LatLng (location.getLatitude(), location.getLongitude());
                            String latitude = "" + location.getLatitude();
                            String longitude = "" + location.getLongitude();
                            String full_location = latitude + ',' + longitude;
                            Map<String, Object> Location = new HashMap<>();
                            Location.put("Location", full_location);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                            final String userID = user.getUid();
                            db.collection("Users")
                                    .document(userID)
                                    .set(Location, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        //                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                        finish();
//                    }
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot written for group");
                                            // create user doc under Users under Group
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document to Groups", e);
                                        }
                                    });
                        }
                    };
                    googleMap.setOnMyLocationChangeListener(myLocationChangeListener);
                    // For dropping a marker at a point on the Map
//                LatLng sydney = new LatLng(-34, 151);
//                Location myLocation = googleMap.getMyLocation();
//                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));
//
//                // For zooming automatically to the location of the marker
//                CameraPosition cameraPosition = new CameraPosition.Builder().target().zoom(12).build();
//                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            });
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                // For dropping a marker at a point on the Map
                LatLng GT = new LatLng(33.7728837, -84.393816);
//                Location myLocation = googleMap.getMyLocation();
                googleMap.addMarker(new MarkerOptions().position(GT).title("Georgia Tech").snippet("Marker Description"));
//
//                // For zooming automatically to the location of the marker
//                CameraPosition cameraPosition = new CameraPosition.Builder().target().zoom(12).build();
//                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        // Get groups of the user
        readGroupNames(new listCallback() {
            @Override
            public void onCallback(ArrayList<String> groupNames) {
                Log.d(TAG, "=DEBUG= Callback Groups: " + groupNames.toString());
                // 2: Acquire a List of User Names (per group)
                for (int i = 0; i < mGroupNames.size(); i++) {
                    // Clears users & status lists (Different for each group)
                    final String groupName = mGroupNames.get(i);
//                    final int finalI = i;
                    readUserNames(new listCallback() {
                        @Override
                        public void onCallback(ArrayList<String> userNames) {
                            Log.d(TAG, "=DEBUG= Callback from User Names");
                            for(int i = 0; i < mUsers.size(); i++){
                                final String userID = mUsers.get(i);
                                final int finalI = i;
                                readUserLocations(new listCallback() {
                                    @Override
                                    public void onCallback(ArrayList<String> data) {
                                        // Add locations to map
//                                        mGroups.add(new GroupStructure(groupName, mUsers, mStatus));
                                        Log.d("Location", "" + mLocations.size());
                                        for(String loc : mLocations) {
                                            Log.d("Location", loc);
                                            // For dropping a marker at a point on the Map
                                            String[] latlong =  loc.split(",");
                                            double latitude = Double.parseDouble(latlong[0]);
                                            double longitude = Double.parseDouble(latlong[1]);
                                            LatLng friend = new LatLng(latitude, longitude);
                                            googleMap.addMarker(new MarkerOptions()
                                                    .position(friend)
                                                    .title(mUsers.get(finalI))
                                                    .snippet(mStatus.get(finalI)));
                                        }
//                                        mMapView.getMapAsync(new OnMapReadyCallback() {
//                                            @Override
//                                            public void onMapReady(GoogleMap googleMap) {
//                                                Log.d("Location", "Map Ready");
//                                                for(String loc : mLocations) {
//                                                    Log.d("Location", loc);
//                                                    // For dropping a marker at a point on the Map
//                                                    String[] latlong =  loc.split(",");
//                                                    double latitude = Double.parseDouble(latlong[0]);
//                                                    double longitude = Double.parseDouble(latlong[1]);
//                                                    LatLng friend = new LatLng(latitude, longitude);
//                                                    googleMap.addMarker(new MarkerOptions().position(friend).title(mUsers.get(finalI)).snippet(mStatus.get(finalI)));
//                                                }
//                                            }
//                                        });
                                    }
                                }, mUserIDs.get(i), mUsers.get(i), finalI);
                            }
                        }
                    }, mGroupIDs.get(i), mGroupNames.get(i));
                }
            }
        });

        return root;
    }

    private void readGroupNames(final listCallback callback) {
        db.collection("Users")
                .document(user.getUid())
                .collection("Groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> groupTask) {
                        if (groupTask.isSuccessful()) {
                            String name, ID;
                            for (QueryDocumentSnapshot doc_group : groupTask.getResult()) { // Each Group a user is connected to
                                // we now have the group's name & ID
                                name = doc_group.getString("Group_Name");
                                Log.d("group", name);
                                mGroupNames.add(name);
                                ID = doc_group.getString("Group_ID");
                                mGroupIDs.add(ID);
                            }
                        } else {
                            Log.d(TAG, "=DEBUG= Error retrieving group docs.");
                        }
                        callback.onCallback(mGroupNames);
                    }
                });
    }

    private void readUserNames(final listCallback callback, String ID, final String name) {
        Log.d(TAG, "=DEBUG= \tPerforming lookup on " + ID);
        db.collection("Groups")
                .document(ID)
                .collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> userTask) {
                        if (userTask.isSuccessful()) {
                            String user, status, location, firebase_id;
                            mUsers.clear();
                            mStatus.clear();
                            mLocations.clear();
                            for (QueryDocumentSnapshot doc_user : userTask.getResult()) {
                                // we now have each of the users' names & status
                                user = doc_user.getString("Display_Name");
                                status = doc_user.getString("Status");
//                                location = doc_user.getString("Location");
                                firebase_id = doc_user.getString("Firebase_ID");
                                // Add to respective lists

                                mUsers.add(user);
                                mUserIDs.add(firebase_id);
                                if (status != null) {
                                    mStatus.add(status);
                                } else {
                                    mStatus.add("");
                                }

                                Log.d(TAG, "=DEBUG= \t\tFound user [" + user + "] : " + status);
                            }
                            Log.d(TAG, "=DEBUG= \tSuccess: Retrieved users.");
                        } else {
                            Log.d(TAG, "=DEBUG= \tError retrieving user docs");
                        }
                        callback.onCallback(mUsers);
                    }
                });
    }

    private void readUserLocations(final listCallback callback, String ID, final String name, final int position) {
        Log.d(TAG, "=DEBUG= \tPerforming lookup on " + ID);
        db.collection("Users")
                .document(ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                String location = document.getString("Location");
                                Log.d("Location", location);
                                if(location == null){
                                    mUserIDs.remove(position);
                                    mUsers.remove(position);
                                } else {
                                    Log.d("Location", "Adding location " + location);
                                    mLocations.add(location);
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                        callback.onCallback(mLocations);
                    }
                });
    }

    public interface listCallback {
        void onCallback(ArrayList<String> data);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}