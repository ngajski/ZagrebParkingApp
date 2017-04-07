package hr.fer.zagrebparkingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CarsActivity extends AppCompatActivity {

    private List<CarInfo> cars;
    private ListView mListView;
    private ArrayAdapter<CarInfo> arrayAdapter;

    private Button mButtonDodaj;
    private Button mButtonZavrsi;

    private FirebaseDatabase database;
    private DatabaseReference carsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);

        database = FirebaseDatabase.getInstance();
        carsRef = database.getReference("cars");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.icon_car);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        cars = new ArrayList<>();

        arrayAdapter = initializeAdapter();

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(arrayAdapter);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener((adapterView, view, i, l) -> openContextMenu(view));


        mButtonDodaj = (Button) findViewById(R.id.dodaj);
        mButtonDodaj.setOnClickListener(view -> {
            alertDialog("Dodavanje novog automobila", -1);
        });

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

        mButtonZavrsi = (Button) findViewById(R.id.zavrsi);
        mButtonZavrsi.setOnClickListener(v -> {
            carsRef.setValue(cars);
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });

    }

    private ArrayAdapter<CarInfo> initializeAdapter() {
        return new ArrayAdapter<CarInfo>(this, R.layout.textview_for_listview, R.id.textViewListView1, cars) {

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


    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }

    }

    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.edit:
                editAction(info);
                return true;

            case R.id.delete:
                deleteAction(info);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteAction(AdapterView.AdapterContextMenuInfo info) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    int index = info.position;
                    cars.remove(index);
                    arrayAdapter.notifyDataSetChanged();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(CarsActivity.this);
        builder.setMessage("Jeste li sigurni da želite obrisati ovaj automobil?")
                .setNegativeButton("Da", dialogClickListener)
                .setPositiveButton("Ne", dialogClickListener)
                .show();
    }

    private void editAction(AdapterView.AdapterContextMenuInfo info) {
        alertDialog("Uređivanje postojećeg automobila", info.position);
    }

    private void alertDialog(String title, long position) {
        LayoutInflater factory = LayoutInflater.from(this);

        final View textEntryView = factory.inflate(R.layout.text_entry, null);

        EditText carName = (EditText) textEntryView.findViewById(R.id.ime_auta);
        EditText registrationNumber = (EditText) textEntryView.findViewById(R.id.registracija);

        if(position >= 0) {
            carName.setText(cars.get((int)position).getName());
            registrationNumber.setText(cars.get((int)position).getRegistrationNumber());
        }

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(R.drawable.icon_car).setTitle(title).setView(textEntryView)
                .setPositiveButton("Prekini",  (dialog, whichButton) -> {
                    processCancelClick(alert, dialog);
                }).setNegativeButton("Spremi", (dialog, whichButton) -> {
                    if(position < 0) {
                        if (TextUtils.isEmpty(carName.getText()) || TextUtils.isEmpty(registrationNumber.getText()) ) {
                            Toast.makeText(alert.getContext(),
                                    "Unesite ispravne podatke, polja ne smiju biti prazna!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            CarInfo carInfo = new CarInfo(carName.getText().toString(),
                                    registrationNumber.getText().toString());
                            cars.add(carInfo);

                            arrayAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    } else {
                        if (TextUtils.isEmpty(carName.getText()) || TextUtils.isEmpty(registrationNumber.getText())) {
                            Toast.makeText(alert.getContext(),
                                    "Unesite ispravne podatke, polja ne smiju biti prazna!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            cars.get((int)position).setName(carName.getText().toString());
                            cars.get((int)position).setName(registrationNumber.getText().toString());

                            arrayAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                });
        alert.setCancelable(false);
        alert.create().show();
    }

    private void processCancelClick(AlertDialog.Builder alert, DialogInterface oldDialog) {
        DialogInterface.OnClickListener dialogClickListener =
                (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

        AlertDialog.Builder builder =
                new AlertDialog.Builder(CarsActivity.this);
        builder.setMessage("Jeste li sigurni da želite prekinuti uređivanje?")
                .setNegativeButton("Da", dialogClickListener)
                .setPositiveButton("Ne", dialogClickListener);
        builder.show();
    }
}
