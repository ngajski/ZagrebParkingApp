package hr.fer.zagrebparkingapp;

import android.location.LocationManager;
import android.view.View;
import android.location.Location;

import java.io.IOException;


/**
 * Created by Pandek on 10.3.2017..
 */

public class FindLocation {

    private Double latitude;
    private Double longitude;
    private String zona;


   /** getLocationButton.setOnClickListener(new OnClickListener() throws IOException{//ime gumba
        @Override
        public void onClick(View v) {
            // instantiate the location manager, note you will need to request permissions in your manifest
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // get the last know location from your location manager.
            Location location= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            // now get the lat/lon from the location and do something with it.
            //nowDoSomethingWith(location.getLatitude(), location.getLongitude());
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            zona = FindZone.provjeri(latitude, longitude);

        }
    });**/
}
