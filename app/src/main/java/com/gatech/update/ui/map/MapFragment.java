package com.gatech.update.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gatech.update.Controller.DrawerActivity;
import com.gatech.update.Controller.LocationService;
import com.gatech.update.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
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
    private Boolean background = false;

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

        // Set title to Map
        ((DrawerActivity) getActivity()).setActionBarTitle("Map");

        mMapView = root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If they don't have permissions on, ask
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            mMapView.onResume();
            Map<String, Object> Location = new HashMap<>();
            Location.put("Location", null);
            db.collection("Users")
                    .document(user.getUid())
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
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.7728837,-84.393816) , 14.0f) );
                }
            });
        } else { // They do, find them
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
//                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
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
                }
            });
            SharedPreferences prefs = getActivity().getSharedPreferences("Prefs", 0);
            SharedPreferences.Editor ed;
            if(!prefs.contains("background")){
                ed = prefs.edit();
                ed.putBoolean("background", true);
                ed.commit();
            }
            background = prefs.getBoolean("background", false);
            if(background) {
                Intent background_service = new Intent(getContext(), LocationService.class);
                getContext().startService(background_service);
            }
        }
        updateMap();
        return root;
    }

    private void readGroupNames(final multiListCallback callback) {
        db.collection("Users")
                .document(user.getUid())
                .collection("Groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> groupTask) {
                        ArrayList<String> gName = new ArrayList<>();
                        ArrayList<String> gID = new ArrayList<>();
                        if (groupTask.isSuccessful()) {
                            for (QueryDocumentSnapshot doc_group : groupTask.getResult()) { // Each Group a user is connected to
                                // we now have the group's name & ID
                                gName.add(doc_group.getString("Group_Name"));
                                gID.add(doc_group.getString("Group_ID"));
                            }
                        } else {
                            Log.d(TAG, "=DEBUG= Error retrieving group docs.");
                        }
                        callback.onCallback(gName, gID, null);
                    }
                });
    }

    private void readUserData(final multiListCallback callback, String ID) {
        Log.d(TAG, "=DEBUG= \tPerforming lookup on " + ID);
        db.collection("Groups")
                .document(ID)
                .collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> userTask) {
                        ArrayList<String> uName = new ArrayList<>();
                        ArrayList<String> uID = new ArrayList<>();
                        ArrayList<String> uStat = new ArrayList<>();
                        if (userTask.isSuccessful()) {
                            String user, status, firebase_id;
                            for (QueryDocumentSnapshot doc_user : userTask.getResult()) {
                                // Add to respective lists
                                uName.add(doc_user.getString("Display_Name"));
                                uID.add(doc_user.getString("Firebase_ID"));

                                status = doc_user.getString("Status");
                                if (status != null) {
                                    uStat.add(status);
                                } else {
                                    uStat.add("");
                                }
                            }
                            Log.d(TAG, "=DEBUG= \tSuccess: Retrieved users.");
                        } else {
                            Log.d(TAG, "=DEBUG= \tError retrieving user docs");
                        }
                        callback.onCallback(uName, uID, uStat);
                    }
                });
    }

    private void readUserLocations(final listCallback callback, String ID) {
        Log.d(TAG, "=DEBUG= \tPerforming lookup on " + ID);
        db.collection("Users")
                .document(ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        ArrayList<String> location = new ArrayList<>();
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                String loc = document.getString("Location");
//                                Log.d("Location", location);
                                if(loc != null){
//                                    Log.d("Location", "Adding location " + loc);
                                    location.add(loc);
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                        callback.onCallback(location);
                    }
                });
    }

    public interface listCallback {
        void onCallback(ArrayList<String> data_X);
    }

    public interface multiListCallback {
        void onCallback(ArrayList<String> data_X, ArrayList<String> data_Y, ArrayList<String> data_Z);
    }

    public void updateMap(){
        // Get groups of the user
        readGroupNames(new multiListCallback() {
            @Override
            public void onCallback(ArrayList<String> gName, ArrayList<String> gID, ArrayList<String> X) {
                Log.d(TAG, "=DEBUG= Callback Groups: " + gName.toString());
                // 2: Acquire a List of User Names (per group)
                for (int i = 0; i < gName.size(); i++) { // for each group
                    readUserData(new multiListCallback() { // Get's all other uses in all groups you're in
                        @Override
                        public void onCallback(final ArrayList<String> uName, ArrayList<String> uID, final ArrayList<String> uStat) {
                            Log.d(TAG, "=DEBUG= Callback from User Names");
                            for(int i = 0; i < uName.size(); i++){ // For each user in each group
                                final int finalI = i;
                                readUserLocations(new listCallback() { // Get's locations of all users

                                    @Override
                                    public void onCallback(ArrayList<String> locations) {
                                        // Add locations to map
                                        Log.d("Location", "" + locations.size());
                                        for(String loc : locations) { // For location of each user of each group
                                            Log.d("Location", loc);
                                            // For dropping a marker at a point on the Map
                                            String[] latlong =  loc.split(",");
                                            double latitude = Double.parseDouble(latlong[0]);
                                            double longitude = Double.parseDouble(latlong[1]);
                                            LatLng friend = new LatLng(latitude, longitude);
                                            if(finalI < uName.size()) {
                                                googleMap.addMarker(new MarkerOptions()
                                                        .position(friend)
                                                        .title(uName.get(finalI))
                                                        .snippet(uStat.get(finalI)));
                                            }
                                        }
                                    }
                                }, uID.get(i));
                            }
                        }
                    }, gID.get(i));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        mMapView.onResume();
        updateMap();
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