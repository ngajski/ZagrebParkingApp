package hr.fer.zagrebparkingapp.model;

import java.util.Calendar;

/**
 * Created by Vilim on 4.5.2017..
 */

public class Payment {

    private Coordinate coordinate;
    private String car;
    private String zone;
    private String paymentTime;
    private double numOfHours;
    private double price;

    public Payment() {
    }

    public Payment(Coordinate coordinate, String car, String zone, String paymentTime, double numOfHours, double price) {
        this.coordinate = coordinate;
        this.car = car;
        this.zone = zone;
        this.paymentTime = paymentTime;
        this.numOfHours = numOfHours;
        this.price = price;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public double getNumOfHours() {
        return numOfHours;
    }

    public void setNumOfHours(double numOfHours) {
        this.numOfHours = numOfHours;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
