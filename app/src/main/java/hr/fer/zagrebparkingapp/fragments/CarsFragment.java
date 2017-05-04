package hr.fer.zagrebparkingapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import hr.fer.zagrebparkingapp.model.Zone;

import static android.app.Activity.RESULT_OK;

public class MapFragment extends Fragment {

    private List<CarInfo> cars;
    private ListView mListView;
    private ArrayAdapter<CarInfo> arrayAdapter;

    private Button mButtonDodaj;
    private Button mButtonZavrsi;

    private FirebaseDatabase database;
    private DatabaseReference carsRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View carView = inflater.inflate(R.layout.activity_cars,container,false);


/*
        setContentView(R.layout.activity_cars);
*/

        database = FirebaseDatabase.getInstance();
        carsRef = database.getReference(FirebaseAuth.getInstance().getCurrentUser().getUid());


        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setLogo(R.drawable.icon_car);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        cars = new ArrayList<>();

        arrayAdapter = initializeAdapter();

        mListView = (ListView) carView.findViewById(R.id.listView);
        mListView.setAdapter(arrayAdapter);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener((adapterView, view, i, l) -> openContextMenu(view));


        mButtonDodaj = (Button) carView.findViewById(R.id.dodaj);
        /*mButtonDodaj.setOnClickListener(view -> {
            alertDialog("Dodavanje novog automobila", -1);
        });*/

        carsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cars.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    cars.add(child.getValue(CarInfo.class));
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mButtonZavrsi = (Button) carView.findViewById(R.id.zavrsi);
        mButtonZavrsi.setOnClickListener(v -> {
            carsRef.setValue(cars);
            Intent intent = new Intent();
            ((MainActivity)getActivity()).setResult(RESULT_OK, intent);
            ((MainActivity)getActivity()).finish();
        });

        return carView;
    }


    private ArrayAdapter<CarInfo> initializeAdapter() {
        return new ArrayAdapter<CarInfo>(getActivity(), R.layout.textview_for_listview, R.id.textViewListView1, cars) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = (TextView) view.findViewById(R.id.textViewListView1);
                TextView text2 = (TextView) view.findViewById(R.id.textViewListView2);

                CarInfo carInfo = cars.get(position);

                if (carInfo.getName() != null && !carInfo.getName().isEmpty()) {
                    text1.setText(carInfo.getName());
                }
                if(carInfo.getRegistrationNumber() != null && !carInfo.getRegistrationNumber().isEmpty()) {
                    text2.setText(carInfo.getRegistrationNumber());
                }

                return view;
            }
        };
    }


    /**
     * Programmatically opens the context menu for a particular {@code view}.
     * The {@code view} should have been added via
     * {@link #registerForContextMenu(View)}.
     *
     * @param view The view to show the context menu for.
     */
    public void openContextMenu(View view) {
        view.showContextMenu();
    }

}
