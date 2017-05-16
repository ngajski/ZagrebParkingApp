package hr.fer.zagrebparkingapp.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;

import java.util.GregorianCalendar;

import hr.fer.zagrebparkingapp.R;
import hr.fer.zagrebparkingapp.model.CarInfo;
import hr.fer.zagrebparkingapp.model.Payment;
import hr.fer.zagrebparkingapp.model.Zone;

public class SplashScreen extends Activity implements MapActivity.LoadingTaskFinishedListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        Bundle extras = getIntent().getExtras();

        int numOfHours = extras.getInt("Hours");
        CarInfo car = (CarInfo) extras.getSerializable("Car");
        Zone zone = (Zone) extras.getSerializable("Zone");
        Payment payment = (Payment) extras.getSerializable("Payment");

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.activity_splash_progress_bar);
        // Start your loading
        new LoadingTask(progressBar, this, getApplicationContext(), car, zone, payment).execute(numOfHours);
        startNotificationService(numOfHours, car, zone, payment);// Pass in whatever you need a url is just an example we don't use it in this tutorial
    }
    // This is the callback for when your async task has finished
    @Override
    public void onTaskFinished() {
        completeSplash();
    }

    private void completeSplash(){
        startApp();
        finish(); // Don't forget to finish this Splash Activity so the user can't return to it!
    }

    private void startApp() {
        Intent intent = new Intent(SplashScreen.this, MapActivity.class);
        startActivity(intent);
    }

    public void startNotificationService(int numOfHours, CarInfo car, Zone zone, Payment payment) {
        Long alertTime;
        if(numOfHours == 1) {
            alertTime = new GregorianCalendar().getTimeInMillis()+(numOfHours)*5*1000;
        } else {
            // Define a time value of 5 seconds
            alertTime = new GregorianCalendar().getTimeInMillis() + (numOfHours) * 5 * 1000 + (numOfHours) * 3000;
        }

        // Define our intention of executing AlertReceiver
        Intent alertIntent = new Intent(this, NotificationService.class);
        alertIntent.putExtra("car", car);
        alertIntent.putExtra("zone", zone);
        alertIntent.putExtra("payment", payment);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in he background or isn't active
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }
}