package hr.fer.zagrebparkingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import hr.fer.zagrebparkingapp.database.DBTestActivity;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
       GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LatLng latLng;
    private Marker currLocationMarker;

    private double currLat;
    private double currLong;

    private String currZone;

    private ArrayList<String> cars;
    private ArrayAdapter<String> dataAdapter;
    private Spinner registrationSpinner;
    private TextView zoneTextView;

    private TextView priceTextView;
    private Button payButton;

    private boolean isStartup = true;

    private Context context;

    private List<Zone> zones;

    public static final String registracija = "ZG6230DV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        registrationSpinner = (Spinner) findViewById(R.id.regSpinner);
        cars = new ArrayList<>();
        zoneTextView = (TextView) findViewById(R.id.zoneEditText);
        priceTextView = (TextView) findViewById(R.id.priceEditText);
        payButton = (Button) findViewById(R.id.payButton);

        context = this.getApplicationContext();

        buildGoogleApiClient();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addItemsToRegistrationSpinner();

        zones = new LinkedList<>(Zone.loadCoordinates(context.getAssets()));

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    generateSMS();
                } catch (IOException e) {
                    Toast.makeText(context, "Neuspjelo plaćanje, IOException", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public String generateSMS() throws IOException{
        /**BufferedReader entry = null;

        try{
            entry = new BufferedReader(new InputStreamReader(context.getAssets().open("treca.txt")));
        } catch(IOException ex){

        }
        String zone = entry.readLine().trim();
        while(!zone.startsWith("currZone")){
            zone = entry.readLine().trim();
        }

        String[] data = zone.split("\t");
        String number = data[1];
        String maxHour = data[2];
        String message = null; //tu idu tablice auta

         **/
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("700103", null, "ZG6230DV", null, null);
        } catch(Exception ex){
            Toast.makeText(context, "Neuspjelo plaćanje, IllegalArgument", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(context, "Uspješno plaćanje", Toast.LENGTH_LONG).show();

        return "SMS uspjesno poslan";
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_OK) {
            List<String> list = data.getStringArrayListExtra("cars");

            cars.clear();

            for(String s : list) {
                cars.add(s);
            }
            dataAdapter.notifyDataSetChanged();
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
//        if (currLocationMarker != null){
//            currLocationMarker.remove();
//        }

        currLat = location.getLatitude();
        currLong = location.getLongitude();
//        currLocationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currLat, currLong))
//                .title("My Location").icon(BitmapDescriptorFactory
//                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        if(isStartup) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLat, currLong),15));
            isStartup = false;
        }
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLat, currLong),15));

//        zoneTextView.setText("Koordinate: (" + currLat + " , " + currLong + ")");

        Coordinate c = new Coordinate(currLat, currLong);

        boolean found = false;
        for(Zone z: zones) {
            if(z.isCoordinateInZone(c)) {
                currZone = z.getName();
                found = true;
                break;
            }
        }

        //currZone = provjeri(currLat, currLong);
        if(found) {
            zoneTextView.setText(currZone);
        } else {
            zoneTextView.setText("Trenutno se ne nalazite ni u jednoj zoni");
        }

    }



    private void addItemsToRegistrationSpinner() {
        cars.add("Dodaj novi automobil...");
        cars.add("list1");

        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, cars);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        registrationSpinner.setAdapter(dataAdapter);
        registrationSpinner.setSelection(1,true);

        registrationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Intent i;
                if (position == 0) {
                    i = newIntent();
                    startActivity(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private Intent newIntent(){
        return new Intent(this, DBTestActivity.class);
    }


    public String provjeri (double latitude, double longitude){
        for(int i = 0; i < 7; i++){//7 zona
            try {
                if(i == 0){
                    BufferedReader ulaz = new BufferedReader(new InputStreamReader(context.getAssets().open("treca.txt")));
//                    AssetManager am = getAssets();
//                    InputStream is = am.open("treca.txt");
//                   // byte[] buffer = Arrays.;
                    //is.read(buffer);
                    boolean nasao = unutra(latitude, longitude, ulaz);
                    if(nasao){
                        return "treca";
                    }
                } else if(i == 1){
                    BufferedReader ulaz = new BufferedReader(new InputStreamReader(context.getAssets().open("cetiri_jedan.txt")));
                    boolean nasao = unutra(latitude, longitude, ulaz);
                    if(nasao){
                        return "cetiri_jedan";
                    }
                } else if(i == 2){
                    BufferedReader ulaz = new BufferedReader(new InputStreamReader(context.getAssets().open("cetiri_dva.txt")));
                    boolean nasao = unutra(latitude, longitude, ulaz);
                    if(nasao){
                        return "cetiri_dva";
                    }
                } else if(i == 3){
                    BufferedReader ulaz = new BufferedReader(new InputStreamReader(context.getAssets().open("paromlin.txt")));
                    boolean nasao = unutra(latitude, longitude, ulaz);
                    if(nasao){
                        return "paromlin";
                    }
                } else if(i == 4){
                    BufferedReader ulaz = new BufferedReader(new InputStreamReader(context.getAssets().open("prva.txt")));
                    boolean nasao = unutra(latitude, longitude, ulaz);
                    if(nasao){
                        return "prva";
                    }
                } else if(i == 5){
                    BufferedReader ulaz = new BufferedReader(new InputStreamReader(context.getAssets().open("jedan_jedan.txt")));
                    boolean nasao = unutra(latitude, longitude, ulaz);
                    if(nasao){
                        return "jedan_jedan";
                    }
                } else if(i == 6) {
                    BufferedReader ulaz = new BufferedReader(new InputStreamReader(context.getAssets().open("druga.txt")));
                    boolean nasao = unutra(latitude, longitude, ulaz);
                    if (nasao) {
                        return "druga";
                    }
                }
            } catch(IOException ex){

            }

        }
        return null;
    }

    public static boolean unutra(double latitude, double longitude, BufferedReader ulaz) throws IOException{
        String line = null;
        boolean pao = false;
        int linija = 0;
//        do{
//                line = ulaz.readLine().trim();

        while((line=ulaz.readLine())!=null){
            line=line.trim();
            String[] podaci = line.split(" ");
            if(podaci.length == 1){
                linija = 0;
                pao = false;
            }
            if(!(podaci.length == 1)){
                linija++;
                double lat = Double.parseDouble(podaci[1]);
                double lon = Double.parseDouble(podaci[2]);
                if((latitude > lat || longitude < lon) && linija == 1){
                    pao = true;
                    line = ulaz.readLine();
                    line = ulaz.readLine();
                    line = ulaz.readLine();
                    continue;
                } else if((latitude > lat || longitude > lon) && linija == 2){
                    pao = true;
                    line = ulaz.readLine();
                    line = ulaz.readLine();
                    continue;
                } else if((latitude < lat || longitude < lon) && linija == 3){
                    pao = true;
                    line = ulaz.readLine();
                    continue;
                } else if((latitude < lat || longitude > lon) && linija == 4){
                    pao = true;
                    continue;
                } else {
                    if(linija == 4 && !pao)
                        return true;
                }
            }
        }


//        } while(!line.equals("kraj"));

        return false;
    }

}
