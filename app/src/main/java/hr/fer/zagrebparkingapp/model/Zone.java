package hr.fer.zagrebparkingapp.model;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Vilim on 24.3.2017..
 */

public class Zone implements Serializable {

    private String name;
    private String price;
    private String number;
    private int hoursAvailable;

    private List<SubZone> subZones;

    public Zone(String name,String price,String number,int hoursAvailable) {
        this.name = name;
        this.price = price;
        this.number = number;
        this.hoursAvailable = hoursAvailable;
        subZones = new LinkedList<>();
    }

    public void addSubZone(SubZone sz) {
        subZones.add(sz);
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

    public String getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {return number;}

    public List<Integer> getHoursAvailable() {
        List<Integer> hourList = new ArrayList<>();

        if (hoursAvailable == 24 && name.endsWith("dan")) {
            hourList.add(hoursAvailable);
        } else  {
            for (int h = 1; h <= hoursAvailable; ++h) {
                hourList.add(h);
            }
        }

        return  hourList;
    }

    @Override
    public String toString() {
        return getName().split(" ")[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Zone zone = (Zone) o;

        if (hoursAvailable != zone.hoursAvailable) return false;
        if (!name.equals(zone.name)) return false;
        if (!price.equals(zone.price)) return false;
        return number.equals(zone.number);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + number.hashCode();
        result = 31 * result + hoursAvailable;
        return result;
    }

    public static List<Zone> loadCoordinates(AssetManager am) {
        try {
            List<Zone> zones = new LinkedList<>();
            List<String> files = Arrays.asList(am.list(""));
            BufferedReader fileReader;

            for(String file : files) {
                if(!file.endsWith(".txt") || file.startsWith("garaze")) continue;
                fileReader = new BufferedReader(new InputStreamReader(am.open(file)));
                String zoneName = file.substring(0, file.indexOf('.'));

                String zonePrice = getZonePrice(zoneName);
                String zoneNumber = getZoneNumber(zoneName);
                int hoursAvailable = getHoursAvailableForZone(zoneName);
                zoneName = getDisplayName(zoneName);

                Zone z = new Zone(zoneName,zonePrice,zoneNumber,hoursAvailable);
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
            case "dva_tri" :
                name = "II.3. zona";
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
                price = "1.5 kn/h";
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
            case "dva_tri" :
                price = "3 kn/h";
                break;
        }

        return price;
    }

    public static String getZoneNumber(String oldName) {
        String number = "";
        switch (oldName) {
            case "prva" :
                number = "700101";
                break;
            case "jedan_jedan" :
                number = "";
                break;
            case "druga" :
                number = "700102";
                break;
            case "treca" :
                number = "700103";
                break;
            case "cetiri_jedan" :
                number = "700105";
                break;
            case "cetiri_dva" :
                number = "700104";
                break;
            case "paromlin" :
                number = "700107";
                break;
            case "dva_tri" :
                number = "700108";
                break;
        }

        return number;
    }

    public static int getHoursAvailableForZone(String name) {
        int available = 24;
        switch (name) {
            case "prva":
                available = 2;
                break;
            case "jedan_jedan":
                available = 0;
                break;
            case "druga":
                available = 3;
                break;
        }

        return  available;
    }

    public static int findZonePosition(Zone zone,List<Zone> zones) {
        if(zone == null) {
            return -1;
        }
        for (int i = 0; i < zones.size(); ++i) {
            if (zone.equals(zones.get(i))) {
                return i;
            }
        }

        return -1;
    }

}
