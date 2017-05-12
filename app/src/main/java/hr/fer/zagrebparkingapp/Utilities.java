package hr.fer.zagrebparkingapp;

import android.content.Context;
import android.telephony.SmsManager;
import android.widget.Toast;

import hr.fer.zagrebparkingapp.model.Payment;

/**
 * Created by Vilim on 12.5.2017..
 */

public class Utilities {

    public static Payment payment;

    public static void generateSMS(Context context, Payment currPayment) {
        payment = currPayment;
        SmsManager sentSMS = SmsManager.getDefault();
        sentSMS.sendTextMessage("700103", null, "ZG6230DV", null, null);
        Toast.makeText(context, "Uspješno plaćanje", Toast.LENGTH_LONG).show();
    }

    public static Payment getPayment() {
        return payment;
    }

    public static void setPayment(Payment payment) {
        Utilities.payment = payment;
    }
}
