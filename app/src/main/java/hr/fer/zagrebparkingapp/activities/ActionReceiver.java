package hr.fer.zagrebparkingapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
                Log.d("Renew", "Renew");
                break;
            case "Close":
                Log.d("Close", "Close");
                break;
        }

    }
}
