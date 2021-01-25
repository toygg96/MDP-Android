package com.example.mdp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import static android.content.ContentValues.TAG;
import com.example.mdp.bluetoothDevice;

import java.util.*;

public class MainActivity extends AppCompatActivity {
    private Button onBluetoothBtn,makeVisibleBtn,offBluetoothBtn,listDeviceBtn,scanBtn;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter adapter;
    private Set bluetoothDevices = new HashSet();
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onBluetoothBtn = (Button) findViewById(R.id.onBluetoothBtn);
        makeVisibleBtn=(Button)findViewById(R.id.makeVisibleBtn);
        offBluetoothBtn=(Button)findViewById(R.id.offBluetoothBtn);
        listDeviceBtn=(Button)findViewById(R.id.listDeviceBtn);
        scanBtn = (Button)findViewById(R.id.scanBtn);
        lv = (ListView)findViewById(R.id.listView);

        onBluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                Log.d(TAG, "Turning bluetooth on");
                onBluetooth(r);
//                Intent i = new Intent(MapsActivity.this,ViewClinicActivity.class);
//                MainActivity.this.startActivity(i);
            }
        });
        makeVisibleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                Log.d(TAG, "Make device visible");
                visibleDevice(r);
            }
        });
        offBluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                Log.d(TAG, "Turning bluetooth off");
                offBluetooth(r);
            }
        });
        listDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                Log.d(TAG, "List Bluetooth devices");
                listPairedDevice(r);
            }
        });

        BA = BluetoothAdapter.getDefaultAdapter();

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                adapter.clear();
                Log.d(TAG, "Scanning for nearby bluetooth devices");
                BA.startDiscovery();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
            }
        });
        checkPermission();
        adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1);
        lv.setAdapter(adapter);
    }

    public void onBluetooth(View v){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void offBluetooth(View v){
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
    }


    public  void visibleDevice(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }


    public void listPairedDevice(View v){
        adapter.clear();
        pairedDevices = BA.getBondedDevices();

        for(BluetoothDevice bt : pairedDevices) adapter.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                Toast.makeText(context, "Starting scan", Toast.LENGTH_LONG).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                Toast.makeText(context, "Scan completed", Toast.LENGTH_LONG).show();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                if (deviceName == null)
                    deviceName = "Unknown device";
                String deviceHardwareAddress = device.getAddress(); // MAC addres
                //Toast.makeText(context, "Found device: " + deviceName, Toast.LENGTH_LONG).show();
                if (adapter.getCount() == 0) {
                    adapter.add("Device name : " + deviceName + "\nDevice Address : " + deviceHardwareAddress);
                    adapter.notifyDataSetChanged();
                } else {
                    boolean duplicateFlag = false;
                    for (int i = 0; i < adapter.getCount(); i++) {
                        String temp = (String) adapter.getItem(i);
                        if (temp.contains(deviceHardwareAddress)) {
                            duplicateFlag = true;
                            break;
                        }
                    }
                    if (!duplicateFlag)  {
                        adapter.add("Device name : " + deviceName + "\nDevice Address : " + deviceHardwareAddress);
                        adapter.notifyDataSetChanged();
                    }
                }

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"Location permissions granted previously");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Granted permissions successfully", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Permissions not granted! App will not run until permissions are granted :(", Toast.LENGTH_LONG).show();
            checkPermission();
        }
    }
}