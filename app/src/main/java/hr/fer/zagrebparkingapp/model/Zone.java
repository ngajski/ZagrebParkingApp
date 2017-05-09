package hr.fer.zagrebparkingapp.model;

import android.content.res.AssetManager;
import android.util.Log;

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
    private String price;

    private List<SubZone> subZones;

    public Zone(String name,String price) {
        this.name = name;
        this.price = price;
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

    public static List<Zone>  loadCoordinates(AssetManager am) {
        try {
            List<Zone> zones = new LinkedList<>();
            List<String> files = Arrays.asList(am.list(""));
            BufferedReader fileReader;

            for(String file : files) {
                if(!file.endsWith(".txt") || file.startsWith("garaze")) continue;
                fileReader = new BufferedReader(new InputStreamReader(am.open(file)));
                String zoneName = file.substring(0, file.indexOf('.'));

                String zonePrice = getZonePrice(zoneName);
                zoneName = getDisplayName(zoneName);

                Zone z = new Zone(zoneName,zonePrice);
                String line;
                List<Coordinate> coordinates = new LinkedList<>();
                boolean isFirstLine = true;
                while((line=fileReader.readLine())!=null) {
                    line=line.trim();
                    List<String> data = Arrays.asList(line.split(" "));
                    if(data.size() != 1) {
                        String type = data.get(0);
                        Log.d("exception", file);
                        System.out.print(data);
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
                fileReader.close();
            }

            return zones;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getDisplayName(String oldName) {
        String name = "";
        switch (oldName) {
            case "prva" :
                name = "I. zona";
                break;
            case "jedan_jedan" :
                name = "I.1. zona";
                break;
            case "druga" :
                name = "II. zona";
                break;
            case "treca" :
                name = "III. zona";
                break;
            case "cetiri_jedan" :
                name = "IV.1. zona";
                break;
            case "cetiri_dva" :
                name = "IV.2. zona";
                break;
            case "paromlin" :
                name = "IV.2.(paromlin) zona";
                break;
        }
        return name;
    }

    private static String getZonePrice(String oldName) {
        String price = "";

        switch (oldName) {
            case "prva" :
                price = "6 kn/h";
                break;
            case "jedan_jedan" :
                price = " - ";
                break;
            case "druga" :
                price = "3 kn/h";
                break;
            case "treca" :
                price = "1,5 kn/h";
                break;
            case "cetiri_jedan" :
                price = "5 kn/dan";
                break;
            case "cetiri_dva" :
                price = "10 kn/dan";
                break;
            case "paromlin" :
                price = "10 kn/dan";
                break;
        }

        return price;
    }

    public String getPrice() {
        return price;
    }
}
