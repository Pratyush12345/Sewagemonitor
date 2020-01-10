package com.example.sewagemonitor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    private EditText name;
    private EditText password;
    //private TextView info;
    private Button login;
    //private int counter =5;
    private FirebaseAuth firebaseAuth;
    DatabaseHelper mDbRef;
    private ProgressDialog progressDialog;
    private EditText nameOfUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        getSupportActionBar().setTitle("Swachh");

        name = findViewById( R.id.user );
        password = (EditText)findViewById( R.id.pass );
        nameOfUser=findViewById(R.id.nameOfUser);
        mDbRef=new DatabaseHelper(this);
        // info = (TextView)findViewById( R.id.text );
        login = (Button)findViewById( R.id.button1 );
        //info.setText("No. of Correct Attempts are 5");
        firebaseAuth=FirebaseAuth.getInstance();

        FirebaseUser user= firebaseAuth.getCurrentUser();

        if(user!=null)
        {
            finish();
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        }


        progressDialog=new ProgressDialog( this );

        login.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(nameOfUser.getText().toString().trim(),name.getText().toString().trim() , password.getText().toString().trim());
            }
        } );


    }

//"cAlW_Xbzc3E:APA91bHgDHJgcjG-D-46Y7YU67gW-nDN12HvryNu6cL8tyHwpfu2HW4tkwO1tyqm3yq4A83gFI2H_qEzGAEJkN0iR27PsSOz9r2XbV3DkVsixTxCAjH_T18dz3Q58i2WlHNmsGh2LLRl"

    private void validate(final String name, String username, String password){

        progressDialog.setMessage( "Wait till the door opens ..." );
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword( username,password ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    boolean isInserted=mDbRef.addNAme(name);
                    if(isInserted==true)
                        Log.d("Inserted successfully","inserted");
                     else
                        Log.d("Not Inserted successfully","not inserted");
                    Toast.makeText( MainActivity.this,"Login Successful",Toast.LENGTH_SHORT ).show();
                    Intent intent = new Intent( MainActivity.this,MapsActivity.class );
                    startActivity(intent);
                    MainActivity.this.finish();
                }
                else{
                    Toast.makeText( MainActivity.this,"Login Failed",Toast.LENGTH_SHORT ).show();
                    //counter--;
                    //info.setText( "No of attempts remaining : "+counter );
                    progressDialog.dismiss();
                    //if(counter==0)
                    //{login.setEnabled( false );}
                }
            }
        } );
    }
}
