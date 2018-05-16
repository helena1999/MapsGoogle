package com.example.opilane.mapsgoogle;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MappActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MappActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean LocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private GoogleMap gKaart;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Kaart on valmis", Toast.LENGTH_SHORT).show();
        gKaart = googleMap;
        if (LocationPermissionsGranted){
            getSeadmeAsukoht();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            }
            return;
        }
        gKaart.setMyLocationEnabled(true);
        gKaart.getUiSettings().setMyLocationButtonEnabled(false);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapp);
        getLocationPermission();
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationPermissionsGranted = true;
                kaivitaKaart();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            LocationPermissionsGranted = false;
                            break;
                        }

                        LocationPermissionsGranted = true;
                        kaivitaKaart();
                    }
                }
            }
        }}

    private void kaivitaKaart() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MappActivity.this);
    }

    private void getSeadmeAsukoht() {
        Log.d(TAG, "SeadmeAsukoht: seadme asukoha tuvastamine");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (LocationPermissionsGranted) {
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MappActivity.this, "Asukoht on tuvastatud", Toast.LENGTH_SHORT).show();
                            Location currentlocation = (Location)task.getResult();
                            liigutaKaamerat (new LatLng(currentlocation.getLatitude(), currentlocation.getLatitude()));
                        }
                        else {
                            Toast.makeText(MappActivity.this, "Asukoht ei olnud võimalik" + "tuvastada", Toast.LENGTH_SHORT).show();

                        }

                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException" + e.getMessage());
        }

    }

    private void liigutaKaamerat(LatLng latLng, float zoom) {
        Log.d(TAG, "lat: " + latLng.latitude + "lng: " + latLng.longitude);
        gKaart.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
}




