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
    private static CarInfo carInfo;
    private static Zone zone;

    public static void generateSMS(Context context, CarInfo car, Zone currZone, Payment currPayment,
            SmsManager sentSMS) {
        payment = currPayment;
        carInfo = car;
        zone = currZone;

        sentSMS.sendTextMessage(zone.getNumber(), null, carInfo.getRegistrationNumber(), null, null);
    }

    public static CarInfo getCarInfo() {
        return carInfo;
    }

    public static void setCarInfo(CarInfo carInfo) {
        Utilities.carInfo = carInfo;
    }

    public static Zone getZone() {
        return zone;
    }

    public static void setZone(Zone zone) {
        Utilities.zone = zone;
    }

    public static Payment getPayment() {
        return payment;
    }

    public static void setPayment(Payment payment) {
        Utilities.payment = payment;
    }
}
