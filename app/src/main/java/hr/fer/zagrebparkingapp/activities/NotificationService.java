package hr.fer.zagrebparkingapp.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import hr.fer.zagrebparkingapp.R;

public class NotificationService extends BroadcastReceiver{

    // Called when a broadcast is made targeting this class
    @Override
    public void onReceive(Context context, Intent intent) {

        createNotification(context, intent);

    }

    public void createNotification(Context context, Intent intent){

        Intent doneIntent = new Intent(context, ActionReceiver.class);
        doneIntent.setAction("Renew");

        Intent closeIntent = new Intent(context, ActionReceiver.class);
        closeIntent.setAction("Close");

        // Define an Intent and an action to perform with it by another application
        PendingIntent notificIntent1 = PendingIntent.getBroadcast(context, 0,
                doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent notificIntent2 = PendingIntent.getBroadcast(context, 0,
                closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Builds a notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.icon_car)
                        .setContentTitle("Produži parking kartu")
                        .setContentText("Parking karta istječe za 15 minuta")
                        .addAction(R.drawable.ic_done_button, "Produži", notificIntent1)
                        .addAction(R.drawable.ic_exit_button, "Zatvori", notificIntent2);

        long[] v = {500,1000};
        mBuilder.setVibrate(v);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(uri);

        // Set the default notification option
        // DEFAULT_SOUND : Make sound
        // DEFAULT_VIBRATE : Vibrate
        // DEFAULT_LIGHTS : Use the default light notification

        // Auto cancels the notification when clicked on in the task bar
        mBuilder.setAutoCancel(false);

        // Gets a NotificationManager which is used to notify the user of the background event
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Post the notification
        mNotificationManager.notify(1, mBuilder.build());

    }
}