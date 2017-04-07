package hr.fer.zagrebparkingapp;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
/**
 * Created by Pandek on 9.3.2017..
 */

public class FindZone {

    private double latitude;
    private double longitude;
    private String zona;

    public FindZone(double latitude, double longitude, String zona){
        this.latitude = latitude;
        this.longitude = longitude;
        this.zona = zona;
    }

    public String getZona() throws IOException{
        this.zona = provjeri(latitude, longitude);
        return this.zona;
    }

    public static String provjeri (double latitude, double longitude) throws IOException{
        for(int i = 0; i < 6; i++){//6 zona

            /**if(i == 0){
                AssetManager am = context.getAssets();
                InputStream is = am.open("test.txt");
               // AssetManager assetManager = getAssets();
                //InputStream ims = assetManager.open("helloworld.txt");
                //InputStream iS = resources.getAssets().open("treca.txt"); // ovo treba importati
               // BufferedReader ulaz = new BufferedReader(new InputStreamReader(iS));
                //System.setIn(new FileInputStream("C:/Users/Pandek/Desktop/treca.txt"));
                //BufferedReader ulaz = new BufferedReader(new InputStreamReader(System.in));
                boolean nasao = unutra(latitude, longitude, ulaz);
                if(nasao){
                    return "treca";
                }
            } else if(i == 1){
                //InputStream iS = resources.getAssets().open("cetiri_jedan.txt"); // ovo treba importati
                BufferedReader ulaz = new BufferedReader(new InputStreamReader(iS));
                //System.setIn(new FileInputStream("C:/Users/Pandek/Desktop/cetiri_jedan.txt"));
                //BufferedReader ulaz = new BufferedReader(new InputStreamReader(System.in));
                boolean nasao = unutra(latitude, longitude, ulaz);
                if(nasao){
                    return "cetiri_jedan";
                }
            } else if(i == 2){
                //InputStream iS = resources.getAssets().open("cetiri_dva.txt"); // ovo treba importati
                BufferedReader ulaz = new BufferedReader(new InputStreamReader(iS));
                //System.setIn(new FileInputStream("C:/Users/Pandek/Desktop/cetiri_dva.txt"));
                //BufferedReader ulaz = new BufferedReader(new InputStreamReader(System.in));
                boolean nasao = unutra(latitude, longitude, ulaz);
                if(nasao){
                    return "cetiri_dva";
                }
            } else if(i == 3){
                // iS = resources.getAssets().open("druga.txt"); // ovo treba importati
                //BufferedReader ulaz = new BufferedReader(new InputStreamReader(iS));
                //System.setIn(new FileInputStream("C:/Users/Pandek/Desktop/druga.txt"));
                //BufferedReader ulaz = new BufferedReader(new InputStreamReader(System.in));
                boolean nasao = unutra(latitude, longitude, ulaz);
                if(nasao){
                    return "druga";
                }
            } else if(i == 4){
                InputStream iS = resources.getAssets().open("jedan_jedan.txt"); // ovo treba importati
                BufferedReader ulaz = new BufferedReader(new InputStreamReader(iS));
                boolean nasao = unutra(latitude, longitude, ulaz);
                if(nasao){
                    return "jedan_jedan";
                }
            } else if(i == 5){
                //nputStream iS = resources.getAssets().open("jedan_jedan.txt"); // ovo treba importati
                //BufferedReader ulaz = new BufferedReader(new InputStreamReader(iS));

                boolean nasao = unutra(latitude, longitude, ulaz);
                if(nasao){
                    return "prva";
                }
            }**/
        }
        return null;
    }

    public static boolean unutra(double latitude, double longitude, BufferedReader ulaz) throws IOException{
        String line = null;
        boolean pao = false;
        int linija = 0;
        do{
            line = ulaz.readLine().trim();
            String[] podaci = line.split(" ");
            if(podaci.length == 1){
                linija = 0;
                pao = false;
            }
            if(!(podaci.length == 1)){
                linija++;
                double lat = Double.parseDouble(podaci[1]);
                double lon = Double.parseDouble(podaci[2]);
                if((latitude > lat || longitude < lon) && linija == 1){
                    pao = true;
                    line = ulaz.readLine();
                    line = ulaz.readLine();
                    line = ulaz.readLine();
                    continue;
                } else if((latitude > lat || longitude > lon) && linija == 2){
                    pao = true;
                    line = ulaz.readLine();
                    line = ulaz.readLine();
                    continue;
                } else if((latitude < lat || longitude < lon) && linija == 3){
                    pao = true;
                    line = ulaz.readLine();
                    continue;
                } else if((latitude < lat || longitude > lon) && linija == 4){
                    pao = true;
                    continue;
                } else {
                    if(linija == 4 && !pao)
                        return true;
                }
            }

        } while(!line.equals("kraj"));

        return false;
    }
}
