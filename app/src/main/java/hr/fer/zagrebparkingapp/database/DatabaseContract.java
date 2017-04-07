package hr.fer.zagrebparkingapp.database;

import android.provider.BaseColumns;

/**
 * Created by nikol on 4/7/2017.
 */

public class DatabaseContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static class DatabaseEntry implements BaseColumns {
        public static final String TABLE_NAME = "Cars";
        public static final String COLUMN_NAME_TITLE = "name";
        public static final String COLUMN_NAME_SUBTITLE = "registration";
    }
}
