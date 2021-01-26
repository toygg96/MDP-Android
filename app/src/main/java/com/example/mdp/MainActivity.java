package com.example.mdp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.*;
import static android.content.ContentValues.TAG;
import com.example.mdp.DeviceListAdapter;

import java.util.*;

public class MainActivity extends AppCompatActivity {
    private Button onBluetoothBtn,makeVisibleBtn,offBluetoothBtn,listDeviceBtn,scanBtn;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private DeviceListAdapter adapter;
    private ListView lv;
    private TextView bluetoothStatusTextView;
    private bluetoothConnectionThread bluetoothThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setLogo(R.drawable.robot);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        onBluetoothBtn = (Button) findViewById(R.id.onBluetoothBtn);
        makeVisibleBtn=(Button)findViewById(R.id.makeVisibleBtn);
        offBluetoothBtn=(Button)findViewById(R.id.offBluetoothBtn);
        listDeviceBtn=(Button)findViewById(R.id.listDeviceBtn);
        scanBtn = (Button)findViewById(R.id.scanBtn);
        lv = (ListView)findViewById(R.id.listView);
        bluetoothStatusTextView = (TextView)findViewById(R.id.bluetoothConnectionStatus);
        adapter = new DeviceListAdapter(this,R.layout.list_item);
        onBluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                //Log.d(TAG, "Turning bluetooth on");
                onBluetooth(r);
//                Intent i = new Intent(MapsActivity.this,ViewClinicActivity.class);
//                MainActivity.this.startActivity(i);
            }
        });
        makeVisibleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                //Log.d(TAG, "Make device visible");
                visibleDevice(r);
            }
        });
        offBluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                //Log.d(TAG, "Turning bluetooth off");
                offBluetooth(r);
            }
        });
        listDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                //Log.d(TAG, "List Bluetooth devices");
                listPairedDevice(r);
            }
        });

        BA = BluetoothAdapter.getDefaultAdapter();
        displayBluetoothState();
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                locationEnabled();
                adapter.clear();
                if (!BA.isEnabled())
                    BA.enable();
                while (BA.getState() != BA.STATE_ON) {}
                Toast.makeText(getApplicationContext(), "Scanning",Toast.LENGTH_LONG).show();
                BA.startDiscovery();
                Log.d(TAG, "Scanning for nearby bluetooth devices");
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
                displayBluetoothState();
            }
        });
        checkPermission();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                boolean result = true;
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                registerReceiver(receiver2, filter);
                pairedDevices = BA.getBondedDevices();
                // based on the item clicked go to the relevant activity
                BluetoothDevice device = (BluetoothDevice) adapter.getItemAtPosition(position);
                Log.d(TAG,"selected address :  " + device.getAddress());
                if (!isDevicePaired(device))
                    result = device.createBond();
                else {
                    bluetoothThread = new bluetoothConnectionThread(device,BA);
                    bluetoothThread.start();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothThread != null)
            bluetoothThread.cancel();
        // Don't forget to unregister the ACTION_FOUND receiver.
        try {
            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(receiver);
            unregisterReceiver(receiver2);
        } catch(IllegalArgumentException e) {
            Log.d(TAG,"Receiver not registered");
        }
    }

    public void displayBluetoothState(){
        if (BA.isEnabled())
            bluetoothStatusTextView.setText("Bluetooth: Turned on\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + BA.getAddress());
        else
            bluetoothStatusTextView.setText("Bluetooth: Turned off\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + BA.getAddress());

    }

    public void onBluetooth(View v){
        if (!BA.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
            bluetoothStatusTextView.setText("Bluetooth: Turned on\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + BA.getAddress());
            BA.enable();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth already turned on", Toast.LENGTH_LONG).show();
        }
    }

    public void offBluetooth(View v){
        BA.disable();
        if (bluetoothThread != null)
            bluetoothThread.cancel();
        bluetoothStatusTextView.setText("Bluetooth: Turned off\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + BA.getAddress());
        Toast.makeText(getApplicationContext(), "Turned off" ,Toast.LENGTH_LONG).show();
        adapter.clear();
    }


    public  void visibleDevice(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }


    public void listPairedDevice(View v){
        adapter.clear();
        pairedDevices = BA.getBondedDevices();

        for(BluetoothDevice bt : pairedDevices) adapter.add(bt);
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    public void addDataToAdapater(BluetoothDevice bd){
        adapter.add(bd);
        adapter.notifyDataSetChanged();
    }

    public boolean isDeviceDuplicate(String deviceHardwareAddress){
        boolean duplicateFlag = false;
        for (int i = 0; i < adapter.getCount(); i++) {
            BluetoothDevice temp = adapter.getItem(i);
            if (temp.getAddress().equals(deviceHardwareAddress)) {
                duplicateFlag = true;
                break;
            }
        }
        return duplicateFlag;
    }

    public boolean isDevicePaired(BluetoothDevice bd) {
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(bd.getAddress())){
                return true;
            }
        }
        return false;
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
                String deviceHardwareAddress = device.getAddress(); // MAC addres
                //Toast.makeText(context, "Found device: " + deviceName, Toast.LENGTH_LONG).show();
                if (adapter.getCount() == 0) {
                    addDataToAdapater(device);
                    //Log.d(TAG,String.valueOf(adapter.getCount()));
                } else {
                    boolean duplicateFlag = isDeviceDuplicate(deviceHardwareAddress);
                    if (!duplicateFlag)
                        addDataToAdapater(device);
                }

            }
        }
    };

    // Create a BroadcastReceiver for ACTION_BOND_STATE_CHANGED.
    private final BroadcastReceiver receiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Log.d(TAG,"Does it reach receiver2?");
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                //Log.d(TAG,"Does this reach here?");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,-1);
                int prevbondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,-1);
                //Log.d(TAG,String.valueOf(bondState) + String.valueOf(prevbondState));
                if (bondState == BluetoothDevice.BOND_BONDED && prevbondState == BluetoothDevice.BOND_BONDING) {
                    bluetoothThread = new bluetoothConnectionThread(device, BA);
                    bluetoothThread.start();
                    Toast.makeText(context, "Paired to selected device! (First time only) Connecting now!" , Toast.LENGTH_LONG).show();
                }
            }
        }
    };

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

    private void locationEnabled () {
        LocationManager lm = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE) ;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(MainActivity. this )
                    .setMessage( "Please enable your GPS before scanning (Only applicable for Android v10)!" )
                    .setPositiveButton( "Settings" , new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                    startActivity( new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) ;
                                }
                            })
                    .setNegativeButton( "Cancel" , null )
                    .show() ;
        }
    }
}