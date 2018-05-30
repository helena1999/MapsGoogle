package com.example.opilane.mapsgoogle;

        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.location.Address;
        import android.location.Geocoder;
        import android.location.Location;
        import android.support.annotation.NonNull;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.Manifest;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.View;
        import android.view.inputmethod.EditorInfo;
        import android.widget.AutoCompleteTextView;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.widget.AdapterView;
        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.GooglePlayServicesRepairableException;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.common.api.PendingResult;
        import com.google.android.gms.common.api.ResultCallback;
        import com.google.android.gms.location.FusedLocationProviderClient;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.location.places.AutocompletePrediction;
        import com.google.android.gms.location.places.Place;
        import com.google.android.gms.location.places.PlaceBuffer;
        import com.google.android.gms.location.places.Places;
        import com.google.android.gms.location.places.ui.PlacePicker;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.LatLngBounds;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;

        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener{
    // log jaoks loome TAGi
    private static final String TAG = "MapActivity";
    // muutujad kus viitame manifesti lubadele
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    //boolean muutuja asukoha lubade saamise kohta
    private Boolean LocationPermissionsGranted = false;
    //asukoha loa request kood (koodi väärtus suvaline)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    // üldine muutuja Google kaardi jaoks
    private GoogleMap gKaart;
    // uus muutuja seadme asukoha jaoks
    private FusedLocationProviderClient fusedLocationProviderClient;
    // üldine muutuja liigutakaamerat zoom jaoks
    private static final float DEFAULT_ZOOM = 15f;
    //otsingu muutuja
    private AutoCompleteTextView otsinguTekst;
    // lisame ikoonide muutujad
    private ImageView gpsNupp, asukohaInfo, asukohaKaart;
    private  PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168),
            new LatLng(71,136));
    // lisame globaalse PlaceInfo muutuja
    private PlaceInfo mPlace;
    private Marker marker;
    private static int PLACE_PICKER_REQUEST = 1;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Kaart on valmis", Toast.LENGTH_SHORT).show();
        gKaart = googleMap;
        if (LocationPermissionsGranted){
            getSeadmeAsukoht();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            gKaart.setMyLocationEnabled(true);
            //kaotasime ära kaardiga tekkinud setlocation nupu kuna loome ise
            gKaart.getUiSettings().setMyLocationButtonEnabled(false);
            kaivita();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapp);
        //seome muutuja id-ga
        otsinguTekst = findViewById(R.id.searchText);
        asukohaInfo = findViewById(R.id.ic_info);
        asukohaKaart = findViewById(R.id.ic_map);
        gpsNupp = findViewById(R.id.ic_gps);
        getLocationPermission();
    }
    private void kaivita(){
        Log.d(TAG, "Lähtestamine");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        otsinguTekst.setOnItemClickListener(autocompleteClickListener);

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter
                (this, mGoogleApiClient, LAT_LNG_BOUNDS, null);

        otsinguTekst.setAdapter(placeAutocompleteAdapter);

        // kirjutame üle reutrn key tegevuse ehk kui vajutada sellele siis on nupu kliki eest
        otsinguTekst.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || keyEvent.getAction() == keyEvent.ACTION_DOWN
                        || keyEvent.getAction() == keyEvent.KEYCODE_ENTER){
                    // meetod otsinguks
                    geoLocate();
                }
                return false;
            }
        });
        asukohaInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: asukoha info nupule klikiti");
                try {
                    if (marker.isInfoWindowShown()){
                        marker.hideInfoWindow();
                    }else {
                        Log.d(TAG, "onClick: Asukoha info" + mPlace.toString());
                        marker.showInfoWindow();
                    }
                }
                catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullpointerException: " +e.getMessage());
                }
            }
        });
        asukohaKaart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int PLACE_PICKER_REQUEST = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                }
                catch (GooglePlayServicesRepairableException e){
                    Log.d(TAG, "GooglePlayServiceRepairableException: " + e.getMessage());

                }
                catch (GooglePlayServicesRepairableException e){
                    Log.d(TAG, "GooglePlayServiceRepairableException: " + e.getMessage());
                }
            }
        });
        gpsNupp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"GPS nupul vajutatud");
                getSeadmeAsukoht();
            }
        });
    }
    protected void onAcitivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PLACE_PICKER_REQUEST){
            if (requestCode == RESULT_OK){
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
               PendingResult<PlaceBuffer> placeBufferPendingResult = Place.GeoDataApi.getPlaceById(mGoogleApiClient, place.getId());
               placeBufferPendingResult.setResultCallback(updatePlaceDetailsCallback);
            }
        }
    }

    private void geoLocate() {
        Log.d(TAG, "Geolocating");
        String otsing = otsinguTekst.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> nimekiri = new ArrayList<>();
        try{
            // meie otsingu stringil on vastete arvuks mis tahame saada 1
            nimekiri = geocoder.getFromLocationName(otsing,1);
        }
        catch(IOException e){
            Log.e(TAG, "Geolocate viga: " + e.getMessage());
        }
        if (nimekiri.size() > 0){
            Address address = nimekiri.get(0);
            Log.d(TAG,"Geolocate leidis ühe asukoha: " + address.toString());
            liigutaKaamerat(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    //meetod kontrollimaks asukoha lubasid
    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED){
                LocationPermissionsGranted = true;
                //meetod mis käivitab meie kaardi
                kaivitaKaart();
            } else {
                ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void kaivitaKaart() {
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().
                findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LocationPermissionsGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            LocationPermissionsGranted = false;
                            break;
                        }
                    }
                    LocationPermissionsGranted = true;
                    //meetod mis lähtestab kaardi
                    kaivitaKaart();
                }
            }
        }
    }
    //meetod seadme asukoha saamiseks
    private void getSeadmeAsukoht(){
        Log.d(TAG,"SeadmeAsukoht: seadme asukoha tuvastamine");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if (LocationPermissionsGranted){
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MapActivity.this,"Asukoht on tuvastatud",
                                    Toast.LENGTH_LONG).show();
                            Location currentLocation = (Location)task.getResult();
                            //kaamera liigutamise meetod ehk liigutakse teise kohta kaardil
                            liigutaKaamerat(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()),DEFAULT_ZOOM, "Minu asukoht");
                        }
                        else{
                            Toast.makeText(MapActivity.this,"Asukoht ei olnud võimalik " +
                                    "tuvastada", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException e){
            Log.e(TAG,"SecurityException" + e.getMessage());
        }
    }
    private void liigutaKaamerat(LatLng latLng, float zoom, PlaceInfo placeInfo) {
        Log.d(TAG,"lat: " + latLng.latitude + "lng: " +latLng.longitude);
        gKaart.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        gKaart.clear();
        if (placeInfo != null){
            try {
               String snippet = "Address : " + placeInfo.getAddress() + "\n" + "Phone number: " + placeInfo.getPhoneNumber() + "\n" + "Website: " + placeInfo.getWebsiteUri() + "\n" + "Prices: " + placeInfo.getRating() + "\n";
               MarkerOptions options = new MarkerOptions().position(latLng).title(placeInfo.getName()).snippet(snippet);
               marker = gKaart.addMarker(options);
            }
            catch (NullPointerException e){
                Log.d(TAG, "moveCamera: NullpointerException" + e.getMessage());
            }
            else{
                gKaart.addMarker(new MarkerOptions().position(latLng));
            }


            }
        }

    }
    private void liigutaKaamerat(LatLng latLng, float zoom, String pealkiri) {
        Log.d(TAG,"lat: " + latLng.latitude + "lng: " +latLng.longitude);
        gKaart.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        // lisame asukohale markeri
        if (!pealkiri.equals("My Location")){
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(pealkiri);
            gKaart.addMarker(markerOptions);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    // Googleplaces api osa
    private AdapterView.OnItemClickListener autocompleteClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final AutocompletePrediction item = placeAutocompleteAdapter.getItem(i);
                    final String placeId = item.getPlaceId();
                    PendingResult<PlaceBuffer> placeBufferPendingResult = Places.GeoDataApi.getPlaceById(
                            mGoogleApiClient, placeId);
                    placeBufferPendingResult.setResultCallback(updatePlaceDetailsCallback);
                }
            };
    private ResultCallback<PlaceBuffer> updatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()){
                Log.d(TAG, "Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);
            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                mPlace.setAddress(place.getAddress().toString());
                mPlace.setId(place.getId().toString());
                mPlace.setLatlng(place.getLatLng());
                mPlace.setRating(place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "Place details: " + mPlace.toString());
            }
            catch(NullPointerException e){
                Log.d(TAG, "NullPointerException: " + e.getMessage());
            }
            liigutaKaamerat(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude),DEFAULT_ZOOM, mPlace);
            places.release();
        }
    };
}





