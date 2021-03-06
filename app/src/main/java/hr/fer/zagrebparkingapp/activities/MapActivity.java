package hr.fer.zagrebparkingapp.activities;

import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.LocationListener;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import hr.fer.zagrebparkingapp.R;
import hr.fer.zagrebparkingapp.Utilities;
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

    private String time;
    private String currentTime;

    private double currLat;
    private double currLong;

    private Zone currentZone;

    //private ArrayAdapter<String> dataAdapter;
    private Spinner registrationSpinner;
    private TextView zoneTextView;

    private TextView priceTextView;
    private Button payButton;

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
    private List<CarInfo> cars;
    private static Marker carMarker;

    private ContextMenuDialogFragment mMenuDialogFragment;

    private TextView userName;
    private TextView userEmail;

    public interface LoadingTaskFinishedListener {
        void onTaskFinished(); // If you want to pass something back to the listener add a param to this method
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("createMethod", "onCreate()");

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

        cars = new ArrayList<>();
        payments = new ArrayList<>();
        database = FirebaseDatabase.getInstance();

        zoneTextView = (TextView) findViewById(R.id.zoneEditText);
        priceTextView = (TextView) findViewById(R.id.priceEditText);
        payButton = (Button) findViewById(R.id.payButton);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registrationSpinner = (Spinner) findViewById(R.id.regSpinner);
        ArrayAdapter<CarInfo> dataAdapter = new ArrayAdapter<CarInfo>(this,
                R.layout.spinner_item, cars);
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

                if(cars.size() == 0) {
                    Intent intent = new Intent("hr.fer.zagrebparkingapp.activities.TabActivity");
                    intent.putExtra("priority", "Cars");
                    startActivity(intent);

                }

                for(CarInfo info : cars) {
                    MapActivity.this.cars.add(info);
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
                payments.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    payments.add(child.getValue(Payment.class));
                }

                if(payments != null && payments.size() != 0) {
                    Calendar calendar1 = Calendar.getInstance();
                    SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
                    String currentDate = formatter1.format(calendar1.getTime());
                    if(payments.get(payments.size()-1).getPaymentTime().equals(currentDate)
                            && payments.get(payments.size()-1).getNumOfHours() != 1) {
                        setParkingMarker();
                    }
                }
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

        if (currentZone == null) {
            payButton.setEnabled(true);
        }
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.carsItem) {
            Intent intent = new Intent("hr.fer.zagrebparkingapp.activities.TabActivity");
            intent.putExtra("priority", "Cars");
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            startActivity(intent);
            return true;
        }

        if(id == R.id.paymentsItem) {
            Intent intent = new Intent("hr.fer.zagrebparkingapp.activities.TabActivity");
            intent.putExtra("priority", "Payments");
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            startActivity(intent);
            return true;
        }

        if (id == R.id.logout) {
            mFirebaseAuth.signOut();
            Auth.GoogleSignInApi.signOut(authGoogleApiClient);
            username = "ANONYMOUS";
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, SignInActivity.class), ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            finish();
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
            moveTaskToBack(true);
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
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setGarageMarkers();
                    }
                });
            }
        }.start();
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
                currentZone = z;
                found = true;
                break;
            }
        }

        if(found) {
            zoneTextView.setText(currentZone.getName());
            priceTextView.setText(currentZone.getPrice());
            payButton.setEnabled(true);
        } else {
            zoneTextView.setText("");
            priceTextView.setText("Ovdje se parkiranje ne naplaćuje");
            payButton.setEnabled(true);
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

    private void setParkingMarker() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(payments != null && payments.size() != 0) {
                    Payment p = payments.get(payments.size() - 1);
                    MarkerOptions mo = new MarkerOptions();
                    Coordinate c = p.getCoordinate();
                    LatLng ll = new LatLng(c.getLattitude(), c.getLongitude());
                    mo.position(ll);
                    mo.title("Automobil " + p.getCar());
                    mo.snippet("Vrijeme parkiranja: " + p.getPaymentTime());

                    Bitmap gm = BitmapFactory.decodeResource(getResources(), R.drawable.car_marker);
                    gm = Bitmap.createScaledBitmap(gm, 70, 70, false);
                    mo.icon(BitmapDescriptorFactory.fromBitmap(gm));
                    carMarker = mMap.addMarker(mo);
                }
            }
        });
    }

    public static void removeParkingMarker() {
        if(carMarker != null)
            carMarker.remove();
    }

    private void alertDialog(String title) {
        LayoutInflater factory = LayoutInflater.from(this);

        final View textEntryView = factory.inflate(R.layout.payment_check, null);

        Spinner zoneSpinner = (Spinner) textEntryView.findViewById(R.id.zoneSpinner);
        TextView carTView = (TextView) textEntryView.findViewById(R.id.currentCar);
        TextView carRegistrationView = (TextView) textEntryView.findViewById(R.id.currentRegistration);
        Spinner hoursSpinner = (Spinner) textEntryView.findViewById(R.id.numOfHours);
        TextView priceTView = (TextView) textEntryView.findViewById(R.id.priceSum);


        CarInfo carInfo = (CarInfo) registrationSpinner.getSelectedItem();
        if(carInfo != null) {
            carTView.setText(registrationSpinner.getSelectedItem().toString());
            carRegistrationView.setText(((CarInfo) registrationSpinner.getSelectedItem()).getRegistrationNumber());
        } else {
            Intent intent = new Intent("hr.fer.zagrebparkingapp.activities.TabActivity");
            intent.putExtra("priority", "Cars");
            startActivity(intent);
            return;
        }

        // init zone spinner
        ArrayAdapter<Zone> zoneAdapter = new ArrayAdapter<Zone>(this,
                R.layout.spinner_item, zones);
        zoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoneSpinner.setAdapter(zoneAdapter);
        int position = Zone.findZonePosition(currentZone,zones);
        zoneSpinner.setSelection(position);

        // init hours spinner
        Zone selectedZone = (Zone) zoneSpinner.getSelectedItem();
        if(selectedZone == null) {
            selectedZone = zones.get(0);
        }
        ArrayAdapter<Integer> hoursSpinnerAdapter = new ArrayAdapter<Integer>(this,
                R.layout.spinner_item,selectedZone.getHoursAvailable());
        hoursSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hoursSpinner.setAdapter(hoursSpinnerAdapter);

        // listen to changes in zone spinner
        zoneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Zone selectedZone = (Zone) zoneSpinner.getSelectedItem();
                hoursSpinnerAdapter.clear();
                hoursSpinnerAdapter.addAll(selectedZone.getHoursAvailable());
                hoursSpinnerAdapter.notifyDataSetChanged();

                priceTView.setText(selectedZone.getPrice());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if(currentZone == null) {
            priceTView.setText(selectedZone.getPrice());
        } else {
            priceTView.setText(currentZone.getPrice());
        }

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(R.drawable.icon_car)
            .setTitle(title)
            .setView(textEntryView)
            .setNegativeButton("Da",  (dialog, whichButton) -> {
                Coordinate coordinate = new Coordinate(currLat, currLong);
                CarInfo car = (CarInfo) registrationSpinner.getSelectedItem();
                Zone zone = (Zone) zoneSpinner.getSelectedItem();

                int numOfHours = (int) hoursSpinner.getSelectedItem();

                Payment payment = new Payment(coordinate, car, zone, numOfHours);

                payments.add(payment);
                paymentsRef.setValue(payments);


                if(numOfHours == 1 || zone.getPrice().endsWith("dan")) {
                    setParkingMarker();
                    SmsManager smsManager = SmsManager.getDefault();
                   // Utilities.generateSMS(this, car, zone, payment, smsManager);
                    startNotificationService(car, zone, payment);
                } else {

                    Intent intent = new Intent(MapActivity.this, SplashScreen.class);
                    intent.putExtra("Hours", numOfHours);
                    intent.putExtra("Car", car);
                    intent.putExtra("Zone", zone);
                    intent.putExtra("Payment", payment);

                    startActivity(intent);
                }

            })
            .setPositiveButton("Ne", (dialog, whichButton) -> {
                dialog.dismiss();
            }
        );
        alert.setCancelable(false);
        alert.create().show();
    }

    public void startNotificationService(CarInfo car, Zone zone, Payment payment) {
        Long alertTime = new GregorianCalendar().getTimeInMillis()+5*1000;
        // Define our intention of executing AlertReceiver
        Intent alertIntent = new Intent(this, NotificationService.class);
        alertIntent.putExtra("car", car);
        alertIntent.putExtra("zone", zone);
        alertIntent.putExtra("payment", payment);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in he background or isn't active
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
