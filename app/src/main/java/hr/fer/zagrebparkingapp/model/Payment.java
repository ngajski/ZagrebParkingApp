package hr.fer.zagrebparkingapp.model;

/**
 * Created by Vilim on 4.5.2017..
 */

public class Payment {

    private Coordinate coordinate;
    private String car;
    private String zone;
    private String paymentTime;
    private int numOfHours;
    private String price;

    public Payment() {
    }

    public Payment(Coordinate coordinate, CarInfo car, Zone zone, String paymentTime, int numOfHours) {
        this.coordinate = coordinate;
        this.car = car.getName();
        this.zone = zone.getName();
        this.paymentTime = paymentTime;
        this.numOfHours = numOfHours;
        this.price = zone.getPrice();
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

    public String getPrice() {
        return price;
    }

}
