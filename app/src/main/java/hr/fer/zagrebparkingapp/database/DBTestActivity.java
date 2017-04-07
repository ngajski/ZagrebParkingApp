package hr.fer.zagrebparkingapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zagrebparkingapp.R;

public class DBTestActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbtest);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("****************","stvaram activity");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readFromDb();
            }
        });

        dbHelper = new DatabaseHelper(getApplicationContext());

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.DatabaseEntry.COLUMN_NAME_TITLE, "BMW");
        values.put(DatabaseContract.DatabaseEntry.COLUMN_NAME_SUBTITLE, "ZG6666ZG");

       /* values.put(DatabaseContract.DatabaseEntry.COLUMN_NAME_TITLE, "Fićo");
        values.put(DatabaseContract.DatabaseEntry.COLUMN_NAME_SUBTITLE, "ZG1111UU");*/

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DatabaseContract.DatabaseEntry.TABLE_NAME, null, values);
        Log.d("long id????????????????",Long.toString(newRowId));
    }

    private void readFromDb() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                DatabaseContract.DatabaseEntry._ID,
                DatabaseContract.DatabaseEntry.COLUMN_NAME_TITLE,
                DatabaseContract.DatabaseEntry.COLUMN_NAME_SUBTITLE
        };

// Filter results WHERE "title" = 'My Title'

// Filter results WHERE "title" = 'My Title'
        String selection = DatabaseContract.DatabaseEntry.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = { "BMW" };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.DatabaseEntry.COLUMN_NAME_SUBTITLE + " DESC";

        Cursor cursor = db.query(
                DatabaseContract.DatabaseEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        List itemIds = new ArrayList<>();
        Log.d("*******************tu","*****************");
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DatabaseContract.DatabaseEntry._ID));
            itemIds.add(itemId);
            String[] names = cursor.getColumnNames();
            Log.d(cursor.getString(cursor.getColumnIndex("name")),"**************");
        }
        cursor.close();
    }
}
