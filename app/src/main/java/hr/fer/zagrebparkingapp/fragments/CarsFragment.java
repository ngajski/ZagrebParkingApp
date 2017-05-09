package hr.fer.zagrebparkingapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import hr.fer.zagrebparkingapp.R;
import hr.fer.zagrebparkingapp.activities.TabActivity;
import hr.fer.zagrebparkingapp.model.CarInfo;

import static android.app.Activity.RESULT_OK;

public class CarsFragment extends Fragment {

    private List<CarInfo> cars;
    private ListView mListView;
    private ArrayAdapter<CarInfo> arrayAdapter;

    private ImageButton mButtonDodaj;
    private ImageButton mButtonZavrsi;

    private FirebaseDatabase database;
    private DatabaseReference carsRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View carView = inflater.inflate(R.layout.fragment_cars,container,false);

        database = FirebaseDatabase.getInstance();
        carsRef = database.getReference(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("cars");

        cars = new ArrayList<>();

        arrayAdapter = initializeAdapter();

        mListView = (ListView) carView.findViewById(R.id.listView);
        mListView.setAdapter(arrayAdapter);
//        registerForContextMenu(mListView);
//        mListView.setOnItemClickListener((adapterView, view, i, l) -> openContextMenu(view));


        mButtonDodaj = (ImageButton) carView.findViewById(R.id.dodaj);
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

        mButtonZavrsi = (ImageButton) carView.findViewById(R.id.zavrsi);
        mButtonZavrsi.setOnClickListener(v -> {
            carsRef.setValue(cars);
            Intent intent = new Intent();
            ((TabActivity)getActivity()).setResult(RESULT_OK, intent);
            ((TabActivity)getActivity()).finish();
        });

        return carView;
    }


    private ArrayAdapter<CarInfo> initializeAdapter() {
        return new ArrayAdapter<CarInfo>(getActivity(), R.layout.list_row, R.id.title, cars) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView title = (TextView) view.findViewById(R.id.title);
                TextView detail = (TextView) view.findViewById(R.id.detail);

                ImageButton edit = (ImageButton) view.findViewById(R.id.editCar);
                ImageButton delete = (ImageButton) view.findViewById(R.id.deleteCar);

                CarInfo carInfo = cars.get(position);

                if (carInfo.getName() != null && !carInfo.getName().isEmpty()) {
                    title.setText(carInfo.getName());
                }
                if(carInfo.getRegistrationNumber() != null && !carInfo.getRegistrationNumber().isEmpty()) {
                    detail.setText(carInfo.getRegistrationNumber());
                }

                edit.setOnClickListener(viewX -> {
                    alertDialog("Uređivanje postojećeg automobila", position);
                });

                delete.setOnClickListener(viewX -> {
                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                int index = position;
                                cars.remove(index);
                                arrayAdapter.notifyDataSetChanged();
                                break;
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Jeste li sigurni da želite obrisati ovaj automobil?")
                            .setNegativeButton("Da", dialogClickListener)
                            .setPositiveButton("Ne", dialogClickListener)
                            .show();
                });

                return view;
            }
        };
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView) {
            MenuInflater inflater = getActivity().getMenuInflater();
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Jeste li sigurni da želite obrisati ovaj automobil?")
                .setNegativeButton("Da", dialogClickListener)
                .setPositiveButton("Ne", dialogClickListener)
                .show();
    }

    private void editAction(AdapterView.AdapterContextMenuInfo info) {
        alertDialog("Uređivanje postojećeg automobila", info.position);
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

    private void alertDialog(String title, long position) {
        LayoutInflater factory = LayoutInflater.from(getActivity());

        final View textEntryView = factory.inflate(R.layout.text_entry, null);

        EditText carName = (EditText) textEntryView.findViewById(R.id.ime_auta);
        EditText registrationNumber = (EditText) textEntryView.findViewById(R.id.registracija);

        if(position >= 0) {
            carName.setText(cars.get((int)position).getName());
            registrationNumber.setText(cars.get((int)position).getRegistrationNumber());
        }

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setIcon(R.drawable.icon_car).setTitle(title).setView(textEntryView)
                .setPositiveButton("Prekini",  (dialog, whichButton) -> {
                    dialog.dismiss();
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
}
