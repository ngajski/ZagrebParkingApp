package hr.fer.zagrebparkingapp.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import hr.fer.zagrebparkingapp.Utilities;
import hr.fer.zagrebparkingapp.model.CarInfo;
import hr.fer.zagrebparkingapp.model.Payment;
import hr.fer.zagrebparkingapp.model.Zone;


public class LoadingTask extends AsyncTask<Integer, Integer, Void> {

    // This is the progress bar you want to update while the task is in progress
    private final ProgressBar progressBar;
    // This is the listener that will be told when this task is finished
    private final MapActivity.LoadingTaskFinishedListener finishedListener;

    private Context context;

    private CarInfo car;
    private Zone zone;
    private Payment payment;


    /**
     * A Loading task that will load some resources that are necessary for the app to start
     *
     * @param progressBar      - the progress bar you want to update while the task is in progress
     * @param finishedListener - the listener that will be told when this task is finished
     */
    public LoadingTask(ProgressBar progressBar, MapActivity.LoadingTaskFinishedListener finishedListener,
                       Context context, CarInfo car, Zone zone, Payment payment) {
        this.progressBar = progressBar;
        this.finishedListener = finishedListener;
        this.context = context;
        this.car = car;
        this.zone = zone;
        this.payment = payment;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        int numOfHours = integers[0];
        try {
            for (int i = 0; i < numOfHours; i++) {
                Utilities.generateSMS(context, car, zone, payment);
                Thread.sleep(3000);
            }
        } catch (Exception ex) {
            Toast.makeText(context, "Neuspjelo plaćanje, IllegalArgument", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        finishedListener.onTaskFinished(); // Tell whoever was listening we have finished
    }
}