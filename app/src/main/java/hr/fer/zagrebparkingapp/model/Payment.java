package hr.fer.zagrebparkingapp.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Vilim on 4.5.2017..
 */

public class Payment {

    private Coordinate coordinate;
    private String car;
    private String zone;
    private String paymentTime;
    private int numOfHours;
    private String hourPrice;
    private double completePrice;

    public Payment() {
    }

    public Payment(Coordinate coordinate, CarInfo car, Zone zone,int numOfHours) {
        this.coordinate = coordinate;
        this.car = car.getName();
        this.zone = zone.getName();
        this.numOfHours = numOfHours;
        this.hourPrice = zone.getPrice();

        DateFormat df = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
        this.paymentTime = df.format(Calendar.getInstance().getTime());

        this.completePrice = numOfHours * Double.parseDouble(hourPrice.split(" ")[0]);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getCar() {
        return car;
    }

    public String getZone() {
        return zone;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public double getNumOfHours() {
        return numOfHours;
    }

    public String getHourPrice() {
        return hourPrice;
    }

    public double getCompletePrice() {
        return completePrice;
    }
}
