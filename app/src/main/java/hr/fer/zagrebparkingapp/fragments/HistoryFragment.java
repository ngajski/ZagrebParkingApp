package hr.fer.zagrebparkingapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

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
import hr.fer.zagrebparkingapp.model.Payment;

import static android.app.Activity.RESULT_OK;

/**
 * Created by nikol on 5/4/2017.
 */

public class HistoryFragment extends Fragment {

    private List<Payment> payments;
    private ListView mListView;
    private ArrayAdapter<Payment> arrayAdapter;

    private ImageButton btnHome;

    private FirebaseDatabase database;
    private DatabaseReference paymentsRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View historyView = inflater.inflate(R.layout.fragment_history,container,false);

        database = FirebaseDatabase.getInstance();
        paymentsRef = database.getReference(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("payments");

        payments = new ArrayList<>();

        arrayAdapter = initializeAdapter();

        mListView = (ListView) historyView.findViewById(R.id.listHistory);
        mListView.setAdapter(arrayAdapter);
        registerForContextMenu(mListView);
//        mListView.setOnItemClickListener((adapterView, view, i, l) -> openContextMenu(view));
//        mListView.setOnItemClickListener((myView,view,j,l) -> openContextMenu(view));
        mListView.setOnItemClickListener((listView, view, position, id) -> {
            LayoutInflater factory = LayoutInflater.from(view.getContext());

            final View textEntryView = factory.inflate(R.layout.payment_check, null);

            TextView zone = (TextView) textEntryView.findViewById(R.id.currentZone);
            TextView car = (TextView) textEntryView.findViewById(R.id.currentCar);
            TextView hours = (TextView) textEntryView.findViewById(R.id.numOfHours);
            TextView price = (TextView) textEntryView.findViewById(R.id.priceSum);
            TextView time = (TextView) textEntryView.findViewById(R.id.timePaid);

            Payment payment = payments.get(position);

            zone.setText(payment.getZone());
            car.setText(payment.getCar());
            hours.setText(payment.getNumOfHours()+"");
            price.setText(payment.getHourPrice()+"");
            time.setText(payment.getPaymentTime());

            final AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
            alert.setIcon(R.drawable.icon_car)
                    .setTitle("Detalji o plaćanju")
                    .setView(textEntryView);
            alert.create().show();

        });

        paymentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                payments.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    payments.add(child.getValue(Payment.class));
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnHome = (ImageButton) historyView.findViewById(R.id.btnHistoryPovratak);
        btnHome.setOnClickListener(v -> {
            paymentsRef.setValue(payments);
            Intent intent = new Intent();
            ((TabActivity)getActivity()).setResult(RESULT_OK, intent);
            ((TabActivity)getActivity()).finish();
        });

        return historyView;
    }


    private ArrayAdapter<Payment> initializeAdapter() {
        return new ArrayAdapter<Payment>(getActivity(), R.layout.list_row_payment, R.id.titlePayment, payments) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView titleTView = (TextView) view.findViewById(R.id.titlePayment);
                TextView zoneTView = (TextView) view.findViewById(R.id.zonePayment);
                TextView timeTView = (TextView) view.findViewById(R.id.timePayment);

                ImageButton delete = (ImageButton) view.findViewById(R.id.deletePayment);

                Payment payment = payments.get(position);

                if (payment.getCar() != null ) {
                    titleTView.setText(payment.getCar() +": " + payment.getCompletePrice() + "kn");
                }

                if (payment.getZone() != null) {
                    zoneTView.setText(payment.getZone() + " " + payment.getHourPrice());
                }

                if (payment.getPaymentTime() != null) {
                    timeTView.setText(payment.getPaymentTime());
                }

                delete.setOnClickListener(viewX -> {
                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                int index = position;
                                payments.remove(index);
                                arrayAdapter.notifyDataSetChanged();
                                break;
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Jeste li sigurni da želite obrisati ovo plaćanje?")
                            .setNegativeButton("Da", dialogClickListener)
                            .setPositiveButton("Ne", dialogClickListener)
                            .show();
                });

                view.setOnClickListener(viewX -> {
                    LayoutInflater factory = LayoutInflater.from(getActivity());

                    final View textEntryView = factory.inflate(R.layout.payment_detail, null);

                    TextView currZone = (TextView) textEntryView.findViewById(R.id.currentZone);
                    TextView currCar = (TextView) textEntryView.findViewById(R.id.currentCar);
                    TextView hours = (TextView) textEntryView.findViewById(R.id.numOfHours);
                    TextView price = (TextView) textEntryView.findViewById(R.id.priceSum);
                    TextView timePaid = (TextView) textEntryView.findViewById(R.id.timePaid);

                    currZone.setText(payment.getZone());
                    currCar.setText(payment.getCar());
                    hours.setText(payment.getNumOfHours()+"");
                    price.setText(payment.getHourPrice()+"");
                    timePaid.setText(payment.getPaymentTime());

                    final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setIcon(R.drawable.bill_icon)
                            .setTitle("Detalji o plaćanju")
                            .setView(textEntryView);
                    alert.create().show();
                });

                return view;
            }
        };
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listHistory) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.edit:
                /*editAction(info);*/
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
                    payments.remove(index);
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

    /*private void editAction(AdapterView.AdapterContextMenuInfo info) {
        alertDialog("Uređivanje postojećeg automobila", info.position);
    }*/

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
