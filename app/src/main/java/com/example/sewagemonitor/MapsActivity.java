package com.example.sewagemonitor;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLoactionMarker;
    public static final int REQUEST_LOCATION_CODE_ = 99;
    double latitude,longitude;
    private double wlevel;
    private UserActivity user;
    private int critical=0,informative=0,normal=0;
    private TextView string1, string2, string3;
    private static final int  notificationid=1223;
    private final String channel_id="personal notification";

    NotificationCompat.Builder notification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        string1=(TextView)findViewById(R.id.textView3);
        string2=(TextView)findViewById(R.id.textView4);
        string3=(TextView)findViewById(R.id.textView5);

        notification = new NotificationCompat.Builder(this,channel_id);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_LOCATION_CODE_:
                if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    //permission granted
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(client==null)
                        {
                            buildGoogleApiClient();
                        }
                    }mMap.setMyLocationEnabled(true);
                }
                else //permission denied
                {
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_LONG).show();
                }
                //return;

        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(false);
        }

    }

    protected synchronized void buildGoogleApiClient()
    {
        client =new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }
    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;

        if (currentLoactionMarker != null) {
            currentLoactionMarker.remove();
        }

            /*String city="";
            Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
            try {
                List<Address> addresses=geocoder.getFromLocation(startlat,startlon,1);
                String address=addresses.get(0).getAddressLine(0);
                city=addresses.get(0).getLocality();
                Log.d("Mylo","Complete address:"+ addresses.toString());
                Log.d("Mylo","address:"+ address);


            } catch (IOException e) {
                e.printStackTrace();
            }*/


        if(client!=null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
        monitoring();
    }
    public void monitoring(){

        //mMap.clear();
        user = new UserActivity();
        database=FirebaseDatabase.getInstance();
        mRef=database.getReference("Jodhpur");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                normal=0;
                informative=0;
                critical=0;
                mMap.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren())  {

                               /* MarkerOptions markerOptions = new MarkerOptions();
                                  markerOptions.position(new LatLng(26.361484,73.020442));
                                  markerOptions.title("Normal");
                                  markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                  markerOptions.snippet("Distance = ");
                                  mMap.addMarker(markerOptions);*/

                    user = ds.getValue(UserActivity.class);
                                latitude = user.getLatitude();
                                longitude = user.getLongitude();
                                wlevel= user.getWlevel();


                                if(wlevel<=25) {
                                    MarkerOptions markerOptions1 = new MarkerOptions();
                                    markerOptions1.position(new LatLng(latitude,longitude));
                                    markerOptions1.title("Normal");
                                    markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    markerOptions1.snippet("level = "+wlevel);
                                    mMap.addMarker(markerOptions1);
                                    normal++;


                                }
                                else if(wlevel>25&& wlevel<=50) {
                                    MarkerOptions markerOptions2 = new MarkerOptions();
                                    markerOptions2.position(new LatLng(latitude,longitude));
                                    markerOptions2.title("Informative");
                                    markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                    markerOptions2.snippet("level = "+wlevel);
                                    mMap.addMarker(markerOptions2);
                                    informative++;


                                }
                                else {

                                    MarkerOptions markerOptions3 = new MarkerOptions();
                                    markerOptions3.position(new LatLng(latitude,longitude));
                                    markerOptions3.title("Critical");
                                    markerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    markerOptions3.snippet("level = "+wlevel);
                                    mMap.addMarker(markerOptions3);
                                    critical++;
                                    //createNotificationChannel();


                                    notification.setSmallIcon(R.drawable.swachh);
                                    notification.setTicker("Regarding sewer in Jodhpur");
                                    notification.setWhen(System.currentTimeMillis());
                                    notification.setContentTitle("Alert!");
                                    notification.setContentText(critical+" Sewers in critical condition, click to get location.");
                                    notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                                    Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(MapsActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    notification.setContentIntent(pendingIntent);

                                    notification.setAutoCancel(true);
                                    NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(MapsActivity.this);
                                    notificationManagerCompat.notify(notificationid,notification.build());




                                }
                    LatLng LatLng = new LatLng(latitude,longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomBy(9));


                }
                string1.setText("Sewers in normal condition "+normal);
                string2.setText("Sewers in informative condition "+informative);
                string3.setText("Sewers in critical condition "+critical);

            }




            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }
    /*public void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            CharSequence name = "Personal notification";
            String description = "Include all personal notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, name, importance);
            notificationChannel.setDescription(description);
            NotificationManager ab = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            ab.createNotificationChannel(notificationChannel);
        }


    }*/



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }

    }
    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE_);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE_);
            }
            return false;

        }
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
