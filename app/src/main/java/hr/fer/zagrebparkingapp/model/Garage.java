package hr.fer.zagrebparkingapp.model;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Vilim on 7.4.2017..
 */

public class Garage {

    private Coordinate coordinate;
    private String name;
    private String adress;
    private String capacity;

    public Garage(Coordinate coordinate, String name, String adress, String capacity) {
        this.coordinate = coordinate;
        this.name = name;
        this.adress = adress;
        this.capacity = capacity;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public static List<Garage> loadCoordinates(AssetManager am) {
        try{
            List<Garage> garages = new LinkedList<>();
            List<String> files = Arrays.asList(am.list(""));
            BufferedReader fileReader;
            for(String file : files) {
                if(file.startsWith("garaze")) {
                    fileReader = new BufferedReader(new InputStreamReader(am.open(file)));
                    String line;
                    List<String> data = new LinkedList<>();
                    while((line=fileReader.readLine())!=null) {
                        data = new LinkedList<>(Arrays.asList(line.split("-")));
                        String[] coordinates = data.get(0).split(" ");
                        Coordinate c = new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
                        Garage g = new Garage(c, data.get(1), data.get(2), data.get(3));
                        garages.add(g);
                    }
                    break;
                }
            }
            return garages;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
