package hr.fer.zagrebparkingapp;

import android.content.Context;
import android.telephony.SmsManager;
import android.widget.Toast;

import hr.fer.zagrebparkingapp.model.Payment;

/**
 * Created by Vilim on 12.5.2017..
 */

public class Utilities {

    public static void generateSMS(Context context, Payment payment) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("700103", null, "ZG6230DV", null, null);
        Toast.makeText(context, "Uspješno plaćanje", Toast.LENGTH_LONG).show();
    }

}
