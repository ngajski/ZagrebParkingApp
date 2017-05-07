package hr.fer.zagrebparkingapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    private Button btnHome;

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
        mListView.setOnItemClickListener((adapterView, view, i, l) -> openContextMenu(view));
        mListView.setOnItemClickListener((myView,view,j,l) -> openContextMenu(view));

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

        btnHome = (Button) historyView.findViewById(R.id.btnHistoryPovratak);
        btnHome.setOnClickListener(v -> {
            paymentsRef.setValue(payments);
            Intent intent = new Intent();
            ((TabActivity)getActivity()).setResult(RESULT_OK, intent);
            ((TabActivity)getActivity()).finish();
        });

        return historyView;
    }


    private ArrayAdapter<Payment> initializeAdapter() {
        return new ArrayAdapter<Payment>(getActivity(), R.layout.textview_for_listview, R.id.textViewListView1, payments) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = (TextView) view.findViewById(R.id.textViewListView1);
                TextView text2 = (TextView) view.findViewById(R.id.textViewListView2);

                Payment payment = payments.get(position);

                if (payment.getCar() != null ) {
                    text1.setText(payment.getCar());
                }

                if (payment.getZone() != null) {
                    text2.setText(payment.getZone() + " " + payment.getPrice());
                }

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
