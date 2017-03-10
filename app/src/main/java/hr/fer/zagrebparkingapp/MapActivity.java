package hr.fer.zagrebparkingapp;

import android.content.Context;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
       GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;

    private LocationRequest mLocationRequest;
    private  GoogleApiClient mGoogleApiClient;
    private LatLng latLng;
    private Marker currLocationMarker;

    private double currLat;
    private double currLong;

    private Spinner registrationSpinner;
    private TextView zoneTextView;
    private TextView priceTextView;
    private Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        registrationSpinner = (Spinner) findViewById(R.id.regSpinner);
        zoneTextView = (TextView) findViewById(R.id.zoneEditText);
        priceTextView = (TextView) findViewById(R.id.priceEditText);
        payButton = (Button) findViewById(R.id.payButton);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addItemsToRegistrationSpinner();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng zagreb = new LatLng(45.814360, 15.977357);
//        mMap.addMarker(new MarkerOptions().position(zagreb).title("Zagreb"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zagreb,15));


        try {
            mMap.setMyLocationEnabled(true);

        } catch (SecurityException ex) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }

       buildGoogleApiClient();

//        mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
//                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mGoogleApiClient.connect();

    }


    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        //Now lets connect to the API
//        mGoogleApiClient.connect();
//    }
    @Override
    protected void onResume() {
      super.onResume();

       if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){

            buildGoogleApiClient();
            mGoogleApiClient.connect();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onConnected(Bundle bundle) throws SecurityException{
//        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//        if (location == null) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//
//        } else {
//            //If everything went fine lets get latitude and longitude
//            currentLatitude = location.getLatitude();
//            currentLongitude = location.getLongitude();
//
//            Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
//        }
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//            /*
//             * Google Play services can resolve some errors it detects.
//             * If the error has a resolution, try sending an Intent to
//             * start a Google Play services activity that can resolve
//             * error.
//             */
//        if (connectionResult.hasResolution()) {
//            try {
//                // Start an Activity that tries to resolve the error
//                connectionResult.startResolutionForResult(this, 9000);
//                    /*
//                     * Thrown if Google Play services canceled the original
//                     * PendingIntent
//                     */
//            } catch (IntentSender.SendIntentException e) {
//                // Log the error
//                e.printStackTrace();
//            }
//        } else {
//                /*
//                 * If no resolution is available, display a dialog to the
//                 * user with the error.
//                 */
//            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
//        }
    }

    /**
     * If locationChanges change lat and long
     *
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
//        currentLatitude = location.getLatitude();
//        currentLongitude = location.getLongitude();
//
//        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
       // mLastLocation = location;

        //remove previous current location Marker
        if (currLocationMarker != null){
            currLocationMarker.remove();
        }

        currLat = location.getLatitude();
        currLong = location.getLongitude();
        currLocationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currLat, currLong))
                .title("My Location").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLat, currLong),15));


        zoneTextView.setText("Koordinate: (" + currLat + " , " + currLong + ")");
    }



    private void addItemsToRegistrationSpinner() {
        List<String> list = new ArrayList<String>();
        list.add("list 1");
        list.add("list 2");
        list.add("list 3");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        registrationSpinner.setAdapter(dataAdapter);
    }



}
