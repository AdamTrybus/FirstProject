package com.sample.notify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.AutocompleteFilter;
import com.google.android.libraries.places.compat.Place;
import com.google.android.libraries.places.compat.ui.PlaceAutocompleteFragment;
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String PRZEDMIOT = "PRZEDMIOT";
    GoogleMap map;
    SupportMapFragment mapFragment;
    Location mlocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int Request_Code = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        setupAutoCompleteFragment();
        mapFragment.getMapAsync(this);
    }
    private void setupAutoCompleteFragment() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
         AutocompleteFilter filter = new AutocompleteFilter.Builder()
                .setCountry("PL")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(filter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mlocation.setLatitude(place.getLatLng().latitude);
                mlocation.setLongitude(place.getLatLng().longitude);
                MarkerOptions markerOptions = new MarkerOptions().position(place.getLatLng()).title(place.getAddress().toString());
                map.clear();
                map.addMarker(markerOptions);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            }

            @Override
            public void onError(Status status) {
                Log.e("Error", status.getStatusMessage());
            }
        });
    }

    public void onClickMap(View view) {
        Intent intent1 = getIntent();
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra(NotificationActivity.PRZEDMIOT, intent1.getStringExtra(PRZEDMIOT));
        if(mlocation.getLatitude()!=0 || mlocation.getLongitude()!=0) {
            intent.putExtra(NotificationActivity.LATITUDE, ""+mlocation.getLatitude());
            intent.putExtra(NotificationActivity.LONGITUDE, ""+mlocation.getLongitude());
        }
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        updateLocationUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Request_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocationUI();
                }
                break;
        }
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        onRequestLocation();
       try {
           map.setMyLocationEnabled(true);
           map.getUiSettings().setMyLocationButtonEnabled(true);
           Task<Location> task = fusedLocationProviderClient.getLastLocation();
           task.addOnSuccessListener(new OnSuccessListener<Location>() {
               @Override
               public void onSuccess(Location location) {
                   if (location != null) {
                       mlocation = location;
                       LatLng latLng = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
                       map.clear();
                       map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                   }
               }});
       }catch (SecurityException e){
           e.printStackTrace();
       }

    }

    private void onRequestLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, Request_Code);
        }
    }
}
