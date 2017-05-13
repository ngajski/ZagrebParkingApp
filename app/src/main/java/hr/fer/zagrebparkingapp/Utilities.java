package hr.fer.zagrebparkingapp;

import android.content.Context;
import android.telephony.SmsManager;
import android.widget.Toast;

import hr.fer.zagrebparkingapp.model.CarInfo;
import hr.fer.zagrebparkingapp.model.Payment;
import hr.fer.zagrebparkingapp.model.Zone;

/**
 * Created by Vilim on 12.5.2017..
 */

public class Utilities {

    private static Payment payment;

    public static void generateSMS(Context context, CarInfo car, Zone zone, Payment currPayment) {
        payment = currPayment;

        SmsManager sentSMS = SmsManager.getDefault();
        sentSMS.sendTextMessage(zone.getNumber(), null, car.getRegistrationNumber(), null, null);
        Toast.makeText(context, "Plaćanje uspijelo", Toast.LENGTH_LONG).show();
    }

    public static Payment getPayment() {
        return payment;
    }

    public static void setPayment(Payment payment) {
        Utilities.payment = payment;
    }
}
