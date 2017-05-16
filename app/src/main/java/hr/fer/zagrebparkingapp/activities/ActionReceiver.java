package hr.fer.zagrebparkingapp.activities;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.GregorianCalendar;

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
                //Utilities.generateSMS(context, Utilities.getCarInfo(), Utilities.getZone(), Utilities.getPayment());
                // Define a time value of 5 seconds
                Long alertTime = new GregorianCalendar().getTimeInMillis() + 5*1000;

                // Define our intention of executing AlertReceiver
                Intent alertIntent = new Intent(context, NotificationService.class);

                // Allows you to schedule for your application to do something at a later date
                // even if it is in he background or isn't active
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                // set() schedules an alarm to trigger
                // Trigger for alertIntent to fire in 5 seconds
                // FLAG_UPDATE_CURRENT : Update the Intent if active
                alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                        PendingIntent.getBroadcast(context, 1, alertIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT));
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1);
                break;
            case "Close":
                // Gets a NotificationManager which is used to notify the user of the background event
                NotificationManager mNotificationManager1 =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager1.cancel(1);
                try {
                    Thread.sleep(3000);
                    MapActivity.removeParkingMarker();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
