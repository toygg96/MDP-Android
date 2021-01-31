package com.example.mdp.Controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.util.Set;

public class BluetoothController {
    private Activity activity;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private DeviceListAdapter adapter;
    private bluetoothConnectionThread.connectThread ct;
    private String connectedDevice = "";
    private bluetoothConnectionThread.AcceptThread at;

    public BluetoothController(Activity activity, BluetoothAdapter BA, DeviceListAdapter adapter){
        this.activity=activity;
        this.BA = BA;
        this.adapter = adapter;
        pairedDevices = BA.getBondedDevices();
        at = new bluetoothConnectionThread().new AcceptThread(activity,BA);
        at.start();
    }

    public String getBluetoothStatus(){
        return("Bluetooth: Turned " + getBluetoothState() +"\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + getAddress(BA.getName()) + "\nConnected to: " + connectedDevice);

    }

    public void rerunBluetoothServer(){
        at.cancel();
        at = null;
        at = new bluetoothConnectionThread().new AcceptThread(activity,BA);
        at.start();
    }

    public String onBluetooth(View v){
        if (!BA.isEnabled()) {
            Toast.makeText(v.getContext(), "Turned on",Toast.LENGTH_LONG).show();
            Boolean result = BA.enable();
            String status = (result) ? "On" : "Off";
           return("Bluetooth: Turned " + status +"\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + getAddress(BA.getName()) + "\nConnected to: " + connectedDevice);
        } else {
            Toast.makeText(v.getContext(), "Bluetooth already turned on", Toast.LENGTH_LONG).show();
        }
        return("Bluetooth: Turned " + getBluetoothState() +"\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + getAddress(BA.getName()) + "\nConnected to: " + connectedDevice);
    }

    public String offBluetooth(View v){
        boolean result = BA.disable();
        String status = (result) ? "Off" : "On";
        if (ct != null)
            ct.cancel();
        Toast.makeText(v.getContext(), "Turned off" ,Toast.LENGTH_LONG).show();
        adapter.clear();
        setConnectedDevice("");
        return("Bluetooth: Turned " + status +"\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + getAddress(BA.getName()) + "\nConnected to: " + connectedDevice);
    }

    public String getBluetoothState(){
        return (BA.isEnabled()) ? "On" : "Off";
    }


    public  void visibleDevice(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        activity.startActivityForResult(getVisible, 0);
    }

    public String getAddress(String name){
        if (name.equalsIgnoreCase("galaxy a70")) {
            return "A8:34:6A:DD:BD:6D";
        }
        else if (name.equalsIgnoreCase("MDP Grp 7")) {
            return "CC:46:4E:E1:D1:D1";
        } else {
            return BA.getAddress();
        }

    }

    public void listPairedDevice(View v){
        adapter.clear();
        pairedDevices = BA.getBondedDevices();

        for(BluetoothDevice bt : pairedDevices) adapter.add(bt);
        Toast.makeText(v.getContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();
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
        pairedDevices = BA.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(bd.getAddress())){
                return true;
            }
        }
        return false;
    }

    public void setConnectedDevice(String status) {
        connectedDevice = status;
    }

    public void attemptConnection(BluetoothDevice device){
        if (ct != null) {
            //Log.d("LIMPEH VALUE",String.valueOf(bluetoothThread.isAlive()));
            ct.cancel();
            //bluetoothThread.setFinishedFlag(false);
            ct = null;
        }
        ct = new bluetoothConnectionThread().new connectThread(activity,device,BA);
        ct.start();
    }

    public void setConnectThreadToNull() {
        ct = null;
    }

    public bluetoothConnectionThread.connectThread getBluetoothThread(){
        return ct;
    }

    public bluetoothConnectionThread.AcceptThread getAcceptedThread() { return at; }

}
