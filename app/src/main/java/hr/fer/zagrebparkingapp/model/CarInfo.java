package hr.fer.zagrebparkingapp.model;

/**
 * Created by Martin on 09-Mar-17.
 */

public class Car {

    private String name;
    private String registrationNumber;

    public Car() {
    }

    public Car(String name, String registrationNumber) {
        this.name = name;
        this.registrationNumber = registrationNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
