package com.myapplicationdev.psp09;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    TextView tv;
    Button start,stop,check;
    FusedLocationProviderClient client;
    private String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);
        start = findViewById(R.id.btnStart);
        stop = findViewById(R.id.btnStop);
        check = findViewById(R.id.btnCheck);


        client = LocationServices.getFusedLocationProviderClient(this);

        if (checkPermission() == true){
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location !=null){
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        String msg = "Last known location when the activity started:\n"+
                                "Latitude: "+lat+"\nLongitude: "+lng;
                        tv.setText(msg);
                    }else{
                        String msg = "No Last Known Location found";
                        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            String msg = "Permisison not granted to retrieve location info";
            Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},0);
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPermission() == true) {
                    LocationRequest mLocationRequest = new LocationRequest();
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setInterval(10000);
                    mLocationRequest.setFastestInterval(5000);
                    mLocationRequest.setSmallestDisplacement(100);

                    LocationCallback mLocationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult != null) {
                                Location data = locationResult.getLastLocation();
                                double lat = data.getLatitude();
                                double lng = data.getLongitude();
                                msg = lat+" , "+lng;
                                String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sample";
                                File folder = new File(folderLocation);
                                if (folder.exists() == false) {
                                    boolean result = folder.mkdir();
                                    if (result == true) {
                                        Log.d("File Read/Write", "Folder created");
                                    }
                                }

                                try {
                                    String folderLocation1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sample";
                                    File targetFile_I = new File(folderLocation1, "sample.txt");
                                    FileWriter writer_I = new FileWriter(targetFile_I, true);
                                    writer_I.write(msg+ "\n");
                                    writer_I.flush();
                                    writer_I.close();
                                    //Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();

                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "Failed to write!", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }

                            }else{
                                String msg = "No Location Updates found";
                                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
                else{
                    String msg = "Permisison not granted to retrieve location info";
                    Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},0);
                }

                Intent intent = new Intent(MainActivity.this,MyService.class);
                startService(intent);
            }
        });


        if (checkPermission()) {
            String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sample";
            File targetFile = new File(folderLocation, "sample.txt");

            if (targetFile.exists() == true) {
                String data = "";
                try {
                    FileReader reader = new FileReader(targetFile);
                    BufferedReader br = new BufferedReader(reader);

                    String line = br.readLine();
                    while (line != null) {
                        data += line + "\n";
                        line = br.readLine();
                    }
                    br.close();
                    reader.close();
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to read!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                Log.d("Content", data);
            }
        } else {
            String msg = "Permission not granted to retrieve location info";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MyService.class);
                stopService(intent);
            }
        });
    }

    private boolean checkPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }
}
