package hr.fer.zagrebparkingapp;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Vilim on 24.3.2017..
 */

public class Zone {

    private String name;
    private List<SubZone> subZones;

    public Zone(String name) {
        this.name = name;
        subZones = new LinkedList<>();
    }

    public void addSubZone(SubZone sz) {
        subZones.add(sz);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SubZone> getSubZones() {
        return subZones;
    }

    public void setSubZones(List<SubZone> subZones) {
        this.subZones = subZones;
    }

    public boolean isCoordinateInZone(Coordinate c) {
        boolean isIn = false;

        for(SubZone sz : subZones) {
            if(isIn= sz.isCoordinateInSubzone(c)){
                break;
            }
        }

        return isIn;
    }

    public static List<Zone> loadCoordinates(AssetManager am) {
        try {
            List<Zone> zones = new LinkedList<>();
            List<String> files = Arrays.asList(am.list(""));
            BufferedReader fileReader;

            for(String file : files) {
                fileReader = new BufferedReader(new InputStreamReader(am.open(file)));
                String zoneName = file.substring(0, file.indexOf('.'));
                Zone z = new Zone(zoneName);
                String line;
                List<Coordinate> coordinates = new LinkedList<>();
                boolean isFirstLine = true;
                while((line=fileReader.readLine())!=null) {
                    line=line.trim();
                    List<String> data = Arrays.asList(line.split(" "));
                    if(data.size() != 1) {
                        String type = data.get(0);
                        Double lat = Double.parseDouble(data.get(1));
                        Double lon = Double.parseDouble(data.get(2));
                        Coordinate c = new Coordinate(lat, lon);
                        coordinates.add(c);
                        //prvi u listi bude GL, drugi GD, treci DL, cetvrti DD
                    } else {
                        if(isFirstLine) {
                            isFirstLine = false;
                        }
                        else {
                            SubZone sz = new SubZone(coordinates);
                            z.addSubZone(sz);
                        }
                        coordinates = new LinkedList<>();
                    }
                }
                zones.add(z);
            }
            return zones;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
