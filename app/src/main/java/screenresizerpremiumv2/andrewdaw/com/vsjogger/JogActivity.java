package screenresizerpremiumv2.andrewdaw.com.vsjogger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class JogActivity extends AppCompatActivity implements LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    String s = "notReady";
    String time;
    Location loc = null;
    Location intermediaryLoc;
    Thread t = null;
    int totalDistance = 0;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jog);


        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = this;

        //getLocationUpdates
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 100, 1, locationListener);
        } catch (SecurityException se) {

        }


        //request a map object
        MapFragment mapFragment1 = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment1.getMapAsync(this);


    }

    /*When the start button is clicked
    *Time is logged
    * Distance is logged
    * Map updates to position
     */
    void doStart(View view) {
        totalDistance = 0;
        //only start if weve got a location
        if (loc != null) {
            //check were not currently running
            if (t == null) {
                //set start location to current location
                intermediaryLoc = loc;
                //set start time
                final int now = (int) (System.currentTimeMillis());
                t = new Thread() {

                    @Override
                    public void run() {
                        try {
                            while (!isInterrupted()) {
                                Thread.sleep(1000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //update gui time
                                        TextView timeElapsed = (TextView) findViewById(R.id.textElapsed);
                                        time = secToTime((((int) (System.currentTimeMillis())) - now) / 1000);
                                        timeElapsed.setText(time);

                                        workDistance();
                                        //update gui distance
                                        TextView dist = (TextView) findViewById(R.id.textDistance);
                                        dist.setText(totalDistance + " meters");
                                        setLoc();

                                        //animate map
                                        LatLng ltlng = new LatLng(loc.getLatitude(), loc.getLongitude());
                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                .target(ltlng)
                                                .zoom(20)
                                                .build();
                                        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cameraPosition);
                                        mMap.animateCamera(cu);


                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                };

                t.start();


            }

        }

    }

    void setLoc() {
        intermediaryLoc = loc;
    }

    void workDistance() {
        totalDistance += intermediaryLoc.distanceTo(loc);
    }

    /*
    *Stop thread and reset
     */
    void doStop(View view) {

        t.interrupt();
        t = null;

    }

    /*
    *Save the stopped run
     */
    void doSave(View view) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("global", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (sharedPreferences.getString("jogs",null) != null) {
            editor.commit();
            editor.putString("jogs",sharedPreferences.getString("jogs",null)+totalDistance+" in "+time+"\n");
            editor.commit();
        }else{
            editor.putString("jogs",totalDistance+" meters in "+time+"\n");
            editor.commit();
        }


    }

    String secToTime(int secs) {
        int minutes;
        int hours;
        String time;
        if (secs <= 60) {
            time = secs + "";
        } else if (secs <= 3600) {
            minutes = secs / 60;
            time = minutes + ":" + secs % 60;
        } else {
            hours = secs / 3600;
            minutes = (secs % 3600) / 60;
            time = hours + ":" + minutes + ":" + secs % 60;

        }

        return time;
    }

    @Override
    public void onLocationChanged(Location loc) {
        this.loc = loc;


        /*------- To get city name from coordinates -------- */
        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {

                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        s = cityName;

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;


        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));


    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.

            mPermissionDenied = false;
        }
    }


}

