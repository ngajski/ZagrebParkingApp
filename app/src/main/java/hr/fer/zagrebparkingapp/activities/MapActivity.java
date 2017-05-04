package hr.fer.zagrebparkingapp.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import com.google.android.gms.location.LocationListener;

import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import hr.fer.zagrebparkingapp.R;
import hr.fer.zagrebparkingapp.model.CarInfo;
import hr.fer.zagrebparkingapp.model.Coordinate;
import hr.fer.zagrebparkingapp.model.Garage;
import hr.fer.zagrebparkingapp.model.Payment;
import hr.fer.zagrebparkingapp.model.Zone;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
       GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LatLng latLng;
    private Marker currLocationMarker;

    private String time;
    private String currentTime;

    private double currLat;
    private double currLong;

    private String currZone;

    private ArrayAdapter<String> dataAdapter;
    private Spinner registrationSpinner;
    private TextView zoneTextView;

    private TextView priceTextView;
    private Button payButton;
    private Button readButton;

    private boolean isStartup = true;

    private Context context;

    private List<Zone> zones;

    private List<Garage> garages;

    FirebaseDatabase database;
    DatabaseReference ref;
    DatabaseReference carsRef;
    DatabaseReference paymentsRef;

    private List<Payment> payments;

    private List<String> carInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        carInfos = new ArrayList<>();
        payments = new LinkedList<>();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference(FirebaseAuth.getInstance().getCurrentUser().getUid());
        carsRef = ref.child("cars");
        paymentsRef = ref.child("payments");

        zoneTextView = (TextView) findViewById(R.id.zoneEditText);
        priceTextView = (TextView) findViewById(R.id.priceEditText);
        payButton = (Button) findViewById(R.id.payButton);

        context = this.getApplicationContext();


        buildGoogleApiClient();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registrationSpinner = (Spinner) findViewById(R.id.regSpinner);
        dataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, carInfos);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        registrationSpinner.setAdapter(dataAdapter);

        zones = new LinkedList<>(Zone.loadCoordinates(context.getAssets()));

        garages = new LinkedList<>(Garage.loadCoordinates(context.getAssets()));

        FloatingActionButton floatingActionButton = (FloatingActionButton)findViewById(R.id.floatAddNewCar);
        floatingActionButton.setImageResource(R.drawable.icon_car);
        floatingActionButton.setOnClickListener(view -> {
            Intent i = newIntent();
            startActivity(i);
        });

        Calendar calendar = Calendar.getInstance();

        currentTime = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE);

//        payButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    //generateSMS();
//                } catch (IOException e) {
//                    Toast.makeText(context, "Neuspjelo plaćanje, IOException", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
        payButton.setOnClickListener(view -> {
            alertDialog("Provjera podataka");
        });




        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataAdapter.clear();

                List<CarInfo> cars = new ArrayList<>();
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    cars.add(child.getValue(CarInfo.class));
                }

                if(cars == null) {
                    carInfos.add("Trenutno nemate odabran auto ->");
                    dataAdapter.notifyDataSetChanged();
                    return;
                }

                for(CarInfo info : cars) {
                    carInfos.add(info.getName() + ":" + info.getRegistrationNumber());
                }
                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", "Failed to read value.", databaseError.toException());
            }
        });

        registrationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Intent i;
                if (dataAdapter.getCount() == 0) {
                    i = newIntent();
                    startActivity(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

  /**  public String generateSMS() throws IOException{
        BufferedReader entry = null;

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


        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("700103", null, "ZG6230DV", null, null);
        } catch(Exception ex){
            Toast.makeText(context, "Neuspjelo plaćanje, IllegalArgument", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(context, "Uspješno plaćanje", Toast.LENGTH_LONG).show();

        return "SMS uspjesno poslan";
    }**/

    public void readSms(Context context) {

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    if(number.equals("700103")){
                        String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                        time = body.substring(body.indexOf(':')-2, body.indexOf(':')+3);
                        Toast.makeText(this, time, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    c.moveToNext();
                }
            }
        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }


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

        setGarageMarkers(mMap);

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
            zoneTextView.setText("Trenutno nemate odabranu zonu");
        }

    }

    private Intent newIntent(){
        return new Intent("hr.fer.zagrebparkingapp.activities.TabActivity");
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


    private void setGarageMarkers(GoogleMap map) {
        for(Garage g : garages) {
            MarkerOptions mo = new MarkerOptions();
            Coordinate c = g.getCoordinate();
            LatLng ll = new LatLng(c.getLattitude(), c.getLongitude());
            mo.position(ll);
            mo.title("Garaža " + g.getName());
            mo.snippet("Kapacitet: " + g.getCapacity() + " mjesta");
            //mo.snippet(g.getCapacity());

            Bitmap gm = BitmapFactory.decodeResource(getResources(), R.drawable.garage_marker);
            gm = Bitmap.createScaledBitmap(gm, 70, 70, false);
            mo.icon(BitmapDescriptorFactory.fromBitmap(gm));
            map.addMarker(mo);
//            CircleOptions co = new CircleOptions();
//            co.center(ll);
//            co.fillColor(Color.GREEN).radius(10);
//            map.addCircle(co);
            //map.addMarker(new MarkerOptions().position(new LatLng(c.getLattitude(), c.getLongitude())));
        }
        //map.setOnMarkerClickListener();
    }

    private void alertDialog(String title) {
        LayoutInflater factory = LayoutInflater.from(this);

        final View textEntryView = factory.inflate(R.layout.payment_check, null);

        Spinner zoneSpinner = (Spinner) textEntryView.findViewById(R.id.zoneSpinner);
        TextView currentCar = (TextView) textEntryView.findViewById(R.id.currentCar);
        TextView numOfHours = (TextView) textEntryView.findViewById(R.id.numOfHours);
        TextView priceSum = (TextView) textEntryView.findViewById(R.id.priceSum);

        List<String> zoneNames = getZoneNamesList();
        ArrayAdapter<String> dataAdapter;
        dataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, zoneNames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoneSpinner.setAdapter(dataAdapter);

        currentCar.setText(registrationSpinner.getSelectedItem().toString());

        numOfHours.setText("novo"); /////////
        priceSum.setText("novo"); /////////

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(R.drawable.icon_car)
            .setTitle(title)
            .setView(textEntryView)
            .setNegativeButton("NE",  (dialog, whichButton) -> {
                dialog.dismiss();
            })
            .setPositiveButton("DA", (dialog, whichButton) -> {
                Coordinate coordinate = new Coordinate(currLat, currLong);
                CarInfo car = (CarInfo) registrationSpinner.getSelectedItem();
                String zone = zoneSpinner.getSelectedItem().toString();
                Calendar paymentTime = Calendar.getInstance();
                double hours = 1;
                double price = 5;
                Payment payment = new Payment(coordinate, car, zone, paymentTime, hours, price);

//                try {
//                    generateSMS();
//                    dialog.dismiss();
//                } catch (IOException e) {
//                    Toast.makeText(context, "Neuspjelo plaćanje, IOException", Toast.LENGTH_LONG).show();
//                }
            }
        );
        alert.setCancelable(false);
        alert.create().show();
    }

    private List<String> getZoneNamesList() {
        List<String> names = new LinkedList<>();
        for(Zone z : zones) {
            names.add(z.getName());
        }
        return names;
    }

}
