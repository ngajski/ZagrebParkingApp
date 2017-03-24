package hr.fer.zagrebparkingapp;

import java.util.List;

/**
 * Created by Vilim on 24.3.2017..
 */

public class SubZone {

    private Coordinate upperLeft;
    private Coordinate upperRight;
    private Coordinate lowerLeft;
    private Coordinate lowerRight;

    private double upperB;//lat
    private double lowerB;//lat
    private double leftB;//long
    private double rightB;//long

    public SubZone(List<Coordinate> coordinates) {
        upperLeft = coordinates.get(0);
        upperRight = coordinates.get(1);
        lowerLeft = coordinates.get(2);
        lowerRight = coordinates.get(3);

        determineBundaries();
    }

    private void determineBundaries() {
        upperB = upperLeft.getLattitude();
        lowerB = lowerRight.getLattitude();
        leftB = upperLeft.getLongitude();
        rightB = lowerRight.getLongitude();
    }

    public Coordinate getUpperLeft() {
        return upperLeft;
    }

    public void setUpperLeft(Coordinate upperLeft) {
        this.upperLeft = upperLeft;
    }

    public Coordinate getUpperRight() {
        return upperRight;
    }

    public void setUpperRight(Coordinate upperRight) {
        this.upperRight = upperRight;
    }

    public Coordinate getLowerLeft() {
        return lowerLeft;
    }

    public void setLowerLeft(Coordinate lowerLeft) {
        this.lowerLeft = lowerLeft;
    }

    public Coordinate getLowerRight() {
        return lowerRight;
    }

    public void setLowerRight(Coordinate lowerRight) {
        this.lowerRight = lowerRight;
    }

    public boolean isCoordinateInSubzone(Coordinate c) {
        double lat = c.getLattitude();
        double lon = c.getLongitude();

        if((lat > lowerB) && (lat < upperB) && (lon > leftB) && (lon < rightB)) {
            return true;
        }

        return false;
    }
}
