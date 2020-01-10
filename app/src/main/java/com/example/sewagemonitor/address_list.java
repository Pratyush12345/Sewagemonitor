package com.example.sewagemonitor;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class address_list extends Fragment {
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private UserActivity user;
    private double wlevel,latitude,longitude;
    private ListView listView;
    private ArrayList<String> arraylist= new ArrayList<>( );
    private int position,counter=0;
    Context context;
    private TextView text1,text2;
    private Button mUpdateButton;
    private String DeviceName="";


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

       Bundle bundle=getArguments();
       position=bundle.getInt("key");

        return inflater.inflate(R.layout.fragment_address,container,false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        text1=view.findViewById(R.id.addresstext);
        text2=view.findViewById(R.id.leveltext);
        mUpdateButton=view.findViewById(R.id.Battery_Update);

        user=new UserActivity();
        database= FirebaseDatabase.getInstance();
        mRef=database.getReference("Jodhpur");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                counter=0;
                for(DataSnapshot ds:dataSnapshot.getChildren()) {
                    user = ds.getValue(UserActivity.class);
                    wlevel = user.getWlevel();
                    latitude = user.getLatitude();
                    longitude = user.getLongitude();

                if(counter==position){
                    String city = "";
                    DeviceName=ds.getKey();
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        String address = addresses.get(0).getAddressLine(0);
                        city = addresses.get(0).getLocality();
                        Log.d("Mylo", "Complete address:" + addresses.toString());
                        Log.d("Mylo", "address:" + address);
                        //addresscom.setText("Destination Address:\n"+address+"\n"+"Distance= "+(int)smallest+"m");
                        text1.setText(address);
                        if(wlevel==0)
                            text2.setText("At Ground Level");
                        else if(wlevel==1)
                            text2.setText("At Normal Level");
                        else if(wlevel==2)
                            text2.setText("At Informative Level");
                        else
                            text2.setText("At Critical Level");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                    counter++;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child(DeviceName).child("Battery").setValue(100);
                mUpdateButton.setEnabled(false);
                Toast.makeText(context, "Battery Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

