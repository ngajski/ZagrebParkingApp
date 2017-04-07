package hr.fer.zagrebparkingapp.model;

/**
 * Created by Vilim on 24.3.2017..
 */

public class Coordinate {

    private double lattitude;
    private double longitude;

    public Coordinate(double lattitude, double longitude) {
        this.lattitude = lattitude;
        this.longitude = longitude;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
