package hr.fer.zagrebparkingapp.activities;

import android.app.NotificationManager;
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
                //Utilities.generateSMS(context, Utilities.getPayment());
                break;
            case "Close":
                // Gets a NotificationManager which is used to notify the user of the background event
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1);
                break;
        }

    }
}
