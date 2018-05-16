package com.example.opilane.mapsgoogle;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ServiceOK()){
            kaivita();
        }

    }
    private void kaivita(){
        Button kaart = findViewById(R.id.nupp);
        kaart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent vaataKaarti = new Intent(MainActivity.this, MappActivity.class);
                startActivity(vaataKaarti);


            }
        });
    }
    public boolean ServiceOK (){
        Log.d(TAG, "ServicesOK: kontrollime google services versiooni");
        int oigeVersioon = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (oigeVersioon == ConnectionResult.SUCCESS){
            Log.d(TAG, "ServiceOK: Google Play Services töötab");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(oigeVersioon)){
            Log.d(TAG, "ServicesOK: esines viga...");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, oigeVersioon, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this, "Kaardi päringute tegemine pole võimalik", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
