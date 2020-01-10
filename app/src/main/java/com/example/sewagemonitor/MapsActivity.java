package com.example.sewagemonitor;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


public class MapsActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private FirebaseDatabase database;
    private DatabaseReference mRef;
    public static final int REQUEST_LOCATION_CODE_ = 99;
    double latitude,longitude;
    private double wlevel;
    private UserActivity user;
    private int critical=0,informative=0,normal=0,groundLevel=0;
    private TextView string1, string2, string3,string4;
    private static final int  notificationid=1223;
    private final String channel_id="personal notification";
    private int flag=0;
    NotificationCompat.Builder notification;
    private DrawerLayout drawer;
    String type="default";
    Toolbar toolbar;
    DatabaseHelper myDbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        myDbRef=new DatabaseHelper(this);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken=instanceIdResult.getToken();
                Log.e("newToken",newToken);
                Cursor res=myDbRef.getData();
                 if(res.getCount()==0){
                     Log.d("Error in geeting data","error fetching data");
                 }
                while (res.moveToNext()) {
                    String nameOfUser = res.getString(1);
                    Log.d("nameOfUser", nameOfUser);
                    database = FirebaseDatabase.getInstance();
                    mRef = database.getReference("Token");
                    mRef.child(nameOfUser).child("Id").setValue(newToken);
                }
            }
        });

        type = getIntent().getStringExtra("From");

        //type = "notifyFrag";
        //Toast.makeText(MapsActivity.this,type, Toast.LENGTH_LONG).show();
        if (type != null) {

            switch (type) {
                case "notifyFrag":
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new critical()).commit();

                    break;
            }
        }


        string1=(TextView)findViewById(R.id.textView3);
        string2=(TextView)findViewById(R.id.textView4);
        string3=(TextView)findViewById(R.id.textView5);
        string4=(TextView)findViewById(R.id.textView6);

        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        //setSupportActionBar(toolbar);
        drawer=findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this, drawer,toolbar,R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //toolbar.setTitle("Home");
        NavigationView navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        notification = new NotificationCompat.Builder(this,channel_id);

        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new home()).commit();
            navigationView.setCheckedItem(R.id.nav_Home);
        }
        monitor();
    }

    public void monitor()
    {
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
                groundLevel=0;
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

                    if(wlevel==1)
                        normal++;

                    else if(wlevel==2)
                        informative++;

                    else if(wlevel==3) {
                        critical++;


                       /* type = getIntent().getStringExtra("From");
                        if(type!=null){
                            switch(type) {
                                case "notifyFrag": break;
                            }
                        }else {
                            createNotificationChannel();


                            notification.setSmallIcon(R.drawable.logo);
                            notification.setTicker("Regarding sewer in Jodhpur");
                            notification.setWhen(System.currentTimeMillis());
                            notification.setContentTitle("Alert!");
                            notification.setContentText(critical + " Sewers in critical condition, click to get location.");
                            notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                            intent.putExtra("From", "notifyFrag");
                            intent.putExtra("value",1);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            //startActivity(intent);
                            PendingIntent pendingIntent = PendingIntent.getActivity(MapsActivity.this, 0, intent, 0);
                            //notification.addAction(R.drawable.ic_sms_black_24dp,"Yes",pendingIntent);
                            notification.setContentIntent(pendingIntent);


                            notification.setAutoCancel(true);
                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MapsActivity.this);
                            notificationManagerCompat.notify(notificationid, notification.build());
                        }*/


                    }
                    else
                    {
                        groundLevel++;
                    }


                }
                string1.setText("Sewers in normal condition: "+normal);
                string2.setText("Sewers in informative condition: "+informative);
                string3.setText("Sewers in critical condition: "+critical);
                string4.setText("Sewers in Ground condition: "+groundLevel);

            }




            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(flag==1)
        {
            Intent intent = new Intent( MapsActivity.this,MapsActivity.class );
            startActivity(intent);
            MapsActivity.this.finish();
            flag=0;
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_Home:
                Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show();
                toolbar.setTitle("Home");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new home()).commit();
                flag=2;
                break;
            case R.id.nav_critical:
                Toast.makeText(this,"Critical level list",Toast.LENGTH_SHORT).show();
                toolbar.setTitle("Critical Sewers");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new critical()).commit();
                flag=1;
                break;
            case R.id.nav_informative:
                Toast.makeText(this,"Informative level list",Toast.LENGTH_SHORT).show();
                toolbar.setTitle("Informative Sewers");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new informative()).commit();
                flag=1;
                break;
            case R.id.nav_normal:
                Toast.makeText(this,"Normal level list",Toast.LENGTH_SHORT).show();
                toolbar.setTitle("Normal Sewers");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new normal()).commit();
                flag=1;
                break;
            case R.id.nav_statistics:
                Toast.makeText(this,"Statistics",Toast.LENGTH_SHORT).show();
                toolbar.setTitle("Statistics");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new statistics()).commit();
                flag=1;
                break;
            case R.id.nav_ground:
                Toast.makeText(this,"Ground",Toast.LENGTH_SHORT).show();
                toolbar.setTitle("Ground Level Sewers");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ground()).commit();
                flag=1;
                break;
            case R.id.nav_status:
                Toast.makeText(this,"Battery Status",Toast.LENGTH_SHORT).show();
                toolbar.setTitle("Battery Status");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new battery_list()).commit();
                flag=1;
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            CharSequence name = "Personal notification";
            String description = "Include all personal notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, name, importance);
            notificationChannel.setDescription(description);
            NotificationManager ab = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            ab.createNotificationChannel(notificationChannel);
        }


    }


}

