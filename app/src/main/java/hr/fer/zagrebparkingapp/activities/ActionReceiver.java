package hr.fer.zagrebparkingapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import hr.fer.zagrebparkingapp.Utilities;

/**
 * Created by Martin on 12-May-17.
 */

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == null) {
            return;
        }
        switch (intent.getAction()) {
            case "Renew":
                Utilities.generateSMS(context, );
                break;
            case "Close":
                Log.d("Close", "Close");
                break;
        }

    }
}
