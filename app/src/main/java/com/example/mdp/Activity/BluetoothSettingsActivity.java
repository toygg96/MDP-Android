package com.example.mdp.Activity;

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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import static android.content.ContentValues.TAG;
import com.example.mdp.Controller.BluetoothController;
import com.example.mdp.Controller.DeviceListAdapter;
import com.example.mdp.R;

public class BluetoothSettingsActivity extends AppCompatActivity {
    private Button makeVisibleBtn,listDeviceBtn,scanBtn,sendBtn;
    private Switch bluetoothSwitch;
    private BluetoothAdapter BA;
    private DeviceListAdapter adapter;
    private ListView lv;
    private TextView bluetoothStatusTextView, incomingTextView;
    private EditText sendMsgInputBox;
    private IntentFilter filter, filter2, filter3, filter4, filter5;
    private boolean offFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate()");
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.scaledrobot);
        makeVisibleBtn=(Button)findViewById(R.id.makeVisibleBtn);
        listDeviceBtn=(Button)findViewById(R.id.listDeviceBtn);
        scanBtn = (Button)findViewById(R.id.scanBtn);
        sendBtn = (Button)findViewById(R.id.sendBtn);
        bluetoothSwitch = (Switch)findViewById(R.id.bluetoothSwitch);
        if (BluetoothController.getConnectedDevice().equalsIgnoreCase(""))
            sendBtn.setEnabled(false);
        else
            sendBtn.setEnabled(true);
        lv = (ListView)findViewById(R.id.listView);
        bluetoothStatusTextView = (TextView)findViewById(R.id.bluetoothConnectionStatus);
        incomingTextView = (TextView)findViewById(R.id.receiveTextView);
        sendMsgInputBox = (EditText)findViewById(R.id.sendTextBox);
        incomingTextView.setMovementMethod(new ScrollingMovementMethod());
        incomingTextView.setText(BluetoothController.getMsgLog());
        // Instantiate the DeviceListAdapter, BluetoothAdapter, BluetoothController
        adapter = new DeviceListAdapter(this,R.layout.list_item);
        BA = BluetoothAdapter.getDefaultAdapter();
        BluetoothController.init(this,BA,adapter);

        if (BluetoothController.getBluetoothState().equalsIgnoreCase("on"))
            bluetoothSwitch.setChecked(true);
        else
            bluetoothSwitch.setChecked(false);

        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()) {
                    offFlag = false;
                    String result = BluetoothController.onBluetooth(buttonView);
                    bluetoothStatusTextView.setText(result);
                } else {
                    offFlag = true;
                    String result = BluetoothController.offBluetooth(buttonView);
                    bluetoothStatusTextView.setText(result);
                }
            }
        });

        makeVisibleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                //Log.d(TAG, "Make device visible");
                BluetoothController.visibleDevice(r);
            }
        });

        listDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                //Log.d(TAG, "List Bluetooth devices");
                BluetoothController.listPairedDevice(r);
            }
        });

        bluetoothStatusTextView.setText(BluetoothController.getBluetoothStatus());
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                scanBtn.setEnabled(false);
                locationEnabled();
                adapter.clear();
                if (!BA.isEnabled())
                    BA.enable();
                while (BA.getState() != BA.STATE_ON) {}
                Toast.makeText(getApplicationContext(), "Scanning",Toast.LENGTH_SHORT).show();
                BA.startDiscovery();
                //Log.d(TAG, "Scanning for nearby bluetooth devices");
                bluetoothStatusTextView.setText(BluetoothController.getBluetoothStatus());
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
                AlertDialog alert;
                // based on the item clicked go to the relevant activity
                BluetoothDevice device = (BluetoothDevice) adapter.getItemAtPosition(position);
//                Log.d(TAG,"selected address :  " + device.getAddress());
//                Log.d(TAG,String.valueOf(v.getContext()));
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                //Uncomment the below code to Set the message and title from the strings.xml file
                builder.setMessage("Do you want to connect to this device?" + device.getName() +"\n" + device.getAddress()).setTitle("Outgoing bluetooth connection request");

                //Setting message manually and performing action on button click
                builder.setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                boolean result = true;
                                if (!BluetoothController.isDevicePaired(device))
                                        result = device.createBond();
                                else
                                    BluetoothController.attemptConnection(device,false);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(),"Operation cancelled!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                //Creating dialog box
                alert = builder.create();
                alert.show();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View r) {
                //Log.d(TAG + "bluetooth",String.valueOf(BluetoothController.getAcceptedThread() == null));
                BluetoothController.sendCmd(sendMsgInputBox.getText().toString());
                sendMsgInputBox.setText("");
            }
        });

    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume()");
        if (BluetoothController.getBluetoothState().equalsIgnoreCase("on"))
            bluetoothSwitch.setChecked(true);
        else
            bluetoothSwitch.setChecked(false);
        // Registering all the receivers
        registerReceivers();
        incomingTextView.setText(BluetoothController.getMsgLog());
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause()");
        unregisterReceiver(receiver);
        unregisterReceiver(receiver2);
        unregisterReceiver(incomingMsgReceiver);
        unregisterReceiver(btConnectionStatusReceiver);
        unregisterReceiver(disconnectedReceiver);
        BluetoothController.saveMsgLog(incomingTextView.getText().toString());
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BluetoothController.getBluetoothThread() != null)
            BluetoothController.getBluetoothThread().cancel();
        // Don't forget to unregister the ACTION_FOUND receiver.
        try {
            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(receiver);
            unregisterReceiver(receiver2);
            unregisterReceiver(incomingMsgReceiver);
            unregisterReceiver(btConnectionStatusReceiver);
            unregisterReceiver(disconnectedReceiver);
        } catch(IllegalArgumentException e) {
            Log.d(TAG,"Receiver not registered");
        }
    }

    public void registerReceivers(){
        if ((filter == null) || (filter2 == null) || (filter3 == null) || (filter4 == null) || (filter5 == null)) {
            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);
            filter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(receiver2, filter2);
            filter3 = new IntentFilter("IncomingMsg");
            registerReceiver(incomingMsgReceiver, filter3);
            filter4 = new IntentFilter("btConnectionStatus");
            registerReceiver(btConnectionStatusReceiver, filter4);
            filter5 = new IntentFilter("disconnectedMsg");
            registerReceiver(disconnectedReceiver, filter5);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,action);
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                Toast.makeText(context, "Starting scan", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && !offFlag) {
                //discovery finishes, dismis progress dialog
                scanBtn.setEnabled(true);
                Toast.makeText(context, "Scan completed", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceHardwareAddress = device.getAddress(); // MAC addres
                //Toast.makeText(context, "Found device: " + deviceName, Toast.LENGTH_SHORT).show();
                if (adapter.getCount() == 0) {
                    BluetoothController.addDataToAdapater(device);
                    //Log.d(TAG,String.valueOf(adapter.getCount()));
                } else {
                    boolean duplicateFlag = BluetoothController.isDeviceDuplicate(deviceHardwareAddress);
                    if (!duplicateFlag)
                        BluetoothController.addDataToAdapater(device);
                }

            }
        }
    };

    // Create a BroadcastReceiver for ACTION_BOND_STATE_CHANGED.
    private final BroadcastReceiver receiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                //Log.d(TAG,"Does this reach here?");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,-1);
                //Log.d(TAG,String.valueOf(bondState));
                int prevbondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,-1);
                //Log.d(TAG,String.valueOf(prevbondState));
                //Log.d(TAG,String.valueOf(bondState) + String.valueOf(prevbondState));
                if (bondState == BluetoothDevice.BOND_BONDED && prevbondState == BluetoothDevice.BOND_BONDING) {
                    BluetoothController.attemptConnection(device,false);
                    Toast.makeText(context, "Paired to selected device! (First time only) Connecting now!" , Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    // Create a BroadcastReceiver for Receive message.
    public BroadcastReceiver incomingMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("receivingMsg");
            incomingTextView.append(msg + "\n");
        }
    };

    public BroadcastReceiver btConnectionStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.clear();
            adapter.notifyDataSetChanged();
            sendBtn.setEnabled(true);
            sendMsgInputBox.setEnabled(true);
            String msg = intent.getStringExtra("Device");
            BluetoothController.setConnectedDevice(msg);
            BluetoothController.setActiveFlag(true);
            bluetoothStatusTextView.setText(BluetoothController.getBluetoothStatus());

        }
    };

    public BroadcastReceiver disconnectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothController.getAcceptedThread().cancel();
            BluetoothController.startBluetoothServer();
            BluetoothController.setConnectThreadToNull();
            BluetoothController.setConnectedDevice("");
            BluetoothController.setActiveFlag(false);
            bluetoothStatusTextView.setText(BluetoothController.getBluetoothStatus());
            sendBtn.setEnabled(false);
            sendMsgInputBox.setEnabled(false);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(BluetoothSettingsActivity.this, RobotPanelActivity.class);
            startActivity(i);
            return true;
        }
        return false;
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
            Toast.makeText(getApplicationContext(), "Granted permissions successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Permissions not granted! App will not run until permissions are granted :(", Toast.LENGTH_SHORT).show();
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
            scanBtn.setEnabled(true);
            new AlertDialog.Builder(BluetoothSettingsActivity. this )
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