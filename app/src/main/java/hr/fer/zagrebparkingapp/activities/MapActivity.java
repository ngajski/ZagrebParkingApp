package hr.fer.zagrebparkingapp.activities;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;

import android.os.AsyncTask;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

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


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
       GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, NavigationView.OnNavigationItemSelectedListener, OnMenuItemClickListener{

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient authGoogleApiClient;
    private GoogleApiClient locationGoogleApiClient;
    private LatLng latLng;
    private Marker currLocationMarker;

    private String time;
    private String currentTime;

    private double currLat;
    private double currLong;

    private String currZone;
    private String currentPrice;

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

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    FirebaseDatabase database;
    DatabaseReference ref;
    DatabaseReference carsRef;
    DatabaseReference paymentsRef;

    private String username;
    private String useremail;

    private List<Payment> payments;
    private List<String> carInfos;
    private List<Marker> carMarkers;

    private ContextMenuDialogFragment mMenuDialogFragment;

    private TextView userName;
    private TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        authGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        buildGoogleApiClient();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            database = FirebaseDatabase.getInstance();
            ref = database.getReference(mFirebaseUser.getUid());
            carsRef = ref.child("cars");
            paymentsRef = ref.child("payments");

            username = mFirebaseUser.getDisplayName();
            useremail = mFirebaseUser.getEmail();

        }
        setWindowTransitions();
        setContentView(R.layout.activity_drawer);

        context = this.getApplicationContext();

        AsyncTaskRunner task = new AsyncTaskRunner();
        task.execute();

        carInfos = new ArrayList<>();
        payments = new ArrayList<>();
        database = FirebaseDatabase.getInstance();

        zoneTextView = (TextView) findViewById(R.id.zoneEditText);
        priceTextView = (TextView) findViewById(R.id.priceEditText);
        payButton = (Button) findViewById(R.id.payButton);

        carMarkers = new LinkedList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registrationSpinner = (Spinner) findViewById(R.id.regSpinner);
        dataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, carInfos);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        registrationSpinner.setAdapter(dataAdapter);

        Calendar calendar = Calendar.getInstance();

        currentTime = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE);

        payButton.setOnClickListener(view -> {
            alertDialog("Provjera podataka");
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setCoolMenu();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name);
        userEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_email);

        userName.setText(username);
        userEmail.setText(useremail);

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
                    //carInfos.add(info.getName() + ":" + info.getRegistrationNumber());
                    carInfos.add(info.getName());
                }
                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error", "Failed to read value.", databaseError.toException());
            }
        });

        paymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                removeParkingMarkers();
                payments.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    payments.add(child.getValue(Payment.class));
                }
                setParkingMarkers();
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

    private void buildGoogleApiClient() {
        locationGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();
    }


    private void setWindowTransitions() {
        Explode ex = new Explode();
        ex.setDuration(1000);
        getWindow().setEnterTransition(ex);

        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setExitTransition(slide);
    }

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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.carsItem) {
            Intent intent = new Intent("hr.fer.zagrebparkingapp.activities.TabActivity");
            intent.putExtra("priority", "Cars");
            startActivity(intent);
            return true;
        }

        if(id == R.id.paymentsItem) {
            Intent intent = new Intent("hr.fer.zagrebparkingapp.activities.TabActivity");
            intent.putExtra("priority", "Payments");
            startActivity(intent);
            return true;
        }

        if (id == R.id.logout) {
            mFirebaseAuth.signOut();
            Auth.GoogleSignInApi.signOut(authGoogleApiClient);
            username = "ANONYMOUS";
            startActivity(new Intent(this, SignInActivity.class), ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            return true;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setCoolMenu() {
        MenuObject close = new MenuObject("Logout");
        close.setResource(R.drawable.ic_logout);

        MenuObject addCar = new MenuObject("Add car");
        addCar.setResource(R.drawable.icon_car);

        MenuObject payments = new MenuObject("Payments");
        payments.setResource(R.drawable.ic_payment);

        List<MenuObject> menuObjects = new ArrayList<>();
        menuObjects.add(close);
        menuObjects.add(addCar);
        menuObjects.add(payments);

        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize(220);
        menuParams.setMenuObjects(menuObjects);
        menuParams.setClosableOutside(true);
        // set other settings to meet your needs
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener((OnMenuItemClickListener) this);
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {

    }

    private class AsyncTaskRunner extends AsyncTask<Object, Object, Void> {

        @Override
        protected Void doInBackground(Object... strings) {
            zones = new LinkedList<>(Zone.loadCoordinates(context.getAssets()));
            garages = new LinkedList<>(Garage.loadCoordinates(context.getAssets()));
            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {

            mMap.setMyLocationEnabled(true);

        } catch (SecurityException ex) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }

        locationGoogleApiClient.connect();
        while(garages == null);
        setGarageMarkers();
        setParkingMarkers();
    }


    @Override
    protected void onResume() {
      super.onResume();

       if (locationGoogleApiClient == null || !locationGoogleApiClient.isConnected()){
            buildGoogleApiClient();
            locationGoogleApiClient.connect();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (locationGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationGoogleApiClient, this);
            locationGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onConnected(Bundle bundle) throws SecurityException{
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        LocationServices.FusedLocationApi.requestLocationUpdates(locationGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * If locationChanges change lat and long
     *
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {


        currLat = location.getLatitude();
        currLong = location.getLongitude();

        if(isStartup) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLat, currLong),15));
            isStartup = false;
        }

        Coordinate c = new Coordinate(currLat, currLong);

        boolean found = false;
        for(Zone z: zones) {
            if(z.isCoordinateInZone(c)) {
                currZone = z.getName();
                currentPrice = z.getPrice();
                found = true;
                break;
            }
        }

        if(found) {
            zoneTextView.setText(currZone);
            priceTextView.setText(currentPrice);
            payButton.setEnabled(true);
        } else {
            zoneTextView.setText("Ovdje se parkiranje ne naplaćuje");
            priceTextView.setText("");
            payButton.setEnabled(false);
        }

    }

    private Intent newIntent(){
        return new Intent("hr.fer.zagrebparkingapp.activities.TabActivity");
    }

    private void setGarageMarkers() {
        for(Garage g : garages) {
            MarkerOptions mo = new MarkerOptions();
            Coordinate c = g.getCoordinate();
            LatLng ll = new LatLng(c.getLattitude(), c.getLongitude());
            mo.position(ll);
            mo.title("Garaža " + g.getName());
            mo.snippet("Kapacitet: " + g.getCapacity() + " mjesta");

            Bitmap gm = BitmapFactory.decodeResource(getResources(), R.drawable.garage_marker);
            gm = Bitmap.createScaledBitmap(gm, 70, 70, false);
            mo.icon(BitmapDescriptorFactory.fromBitmap(gm));
            mMap.addMarker(mo);
        }
    }

    private void setParkingMarkers() {
        for(Payment p : payments) {
            MarkerOptions mo = new MarkerOptions();
            Coordinate c = p.getCoordinate();
            LatLng ll = new LatLng(c.getLattitude(), c.getLongitude());
            mo.position(ll);
            mo.title("Automobil " + p.getCar());
            mo.snippet("Vrijeme parkiranja: " + p.getPaymentTime());

            Bitmap gm = BitmapFactory.decodeResource(getResources(), R.drawable.car_marker);
            gm = Bitmap.createScaledBitmap(gm, 70, 70, false);
            mo.icon(BitmapDescriptorFactory.fromBitmap(gm));
            carMarkers.add(mMap.addMarker(mo));
        }
    }

    private void removeParkingMarkers() {
        for(Marker m : carMarkers) {
            m.remove();
        }
        carMarkers.clear();
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
                String car =  registrationSpinner.getSelectedItem().toString();
                String zone = zoneSpinner.getSelectedItem().toString();
                //Calendar paymentTime = Calendar.getInstance();
                String time = "sada";
                double hours = 1;
                double price = 5;
                Payment payment = new Payment(coordinate, car, zone, time, hours, price);
                payments.add(payment);
                paymentsRef.setValue(payments);
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
