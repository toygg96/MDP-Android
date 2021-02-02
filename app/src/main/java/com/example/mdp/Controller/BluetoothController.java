package com.example.mdp.Controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.util.Set;

public class BluetoothController {
    private static Activity activity;
    private static BluetoothAdapter BA;
    private static Set<BluetoothDevice> pairedDevices;
    private static DeviceListAdapter adapter;
    private static bluetoothConnectionThread.connectThread ct;
    private static String connectedDevice = "";
    private static bluetoothConnectionThread.AcceptThread at;

    public static void init(Activity activity1, BluetoothAdapter bAdapter, DeviceListAdapter dlAdapter) {
        activity= activity1;
        BA = bAdapter;
        adapter = dlAdapter;
        pairedDevices = BA.getBondedDevices();
        startBluetoothServer();
    }

    public static String getBluetoothStatus(){
        return("Bluetooth: Turned " + getBluetoothState() +"\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + getAddress(BA.getName()) + "\nConnected to: " + connectedDevice);

    }

    public static void startBluetoothServer(){
        at = new bluetoothConnectionThread().new AcceptThread(activity,BA);
        at.start();

    }
    public static void restartBluetoothServer(){
        at.cancel();
        at = null;
        at = new bluetoothConnectionThread().new AcceptThread(activity,BA);
        at.start();
    }

    public static String onBluetooth(View v){
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

    public static String offBluetooth(View v){
        boolean result = BA.disable();
        String status = (result) ? "Off" : "On";
        if (ct != null)
            ct.cancel();
        Toast.makeText(v.getContext(), "Turned off" ,Toast.LENGTH_LONG).show();
        adapter.clear();
        setConnectedDevice("");
        return("Bluetooth: Turned " + status +"\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + getAddress(BA.getName()) + "\nConnected to: " + connectedDevice);
    }

    public static String getBluetoothState(){
        return (BA.isEnabled()) ? "On" : "Off";
    }


    public static void visibleDevice(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        activity.startActivityForResult(getVisible, 0);
    }

    public static String getAddress(String name){
        if (name.equalsIgnoreCase("galaxy a70")) {
            return "A8:34:6A:DD:BD:6D";
        }
        else if (name.equalsIgnoreCase("MDP Grp 7")) {
            return "CC:46:4E:E1:D1:D1";
        } else {
            return BA.getAddress();
        }

    }

    public static void listPairedDevice(View v){
        adapter.clear();
        pairedDevices = BA.getBondedDevices();

        for(BluetoothDevice bt : pairedDevices) adapter.add(bt);
        Toast.makeText(v.getContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    public static void addDataToAdapater(BluetoothDevice bd){
        adapter.add(bd);
        adapter.notifyDataSetChanged();
    }

    public static boolean isDeviceDuplicate(String deviceHardwareAddress){
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

    public static boolean isDevicePaired(BluetoothDevice bd) {
        pairedDevices = BA.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(bd.getAddress())){
                return true;
            }
        }
        return false;
    }

    public static void setConnectedDevice(String status) {
        connectedDevice = status;
    }

    public static void attemptConnection(BluetoothDevice device){
        if (ct != null) {
            //Log.d("LIMPEH VALUE",String.valueOf(bluetoothThread.isAlive()));
            ct.cancel();
            //bluetoothThread.setFinishedFlag(false);
            ct = null;
        }
        ct = new bluetoothConnectionThread().new connectThread(activity,device,BA);
        ct.start();
    }

    public static void setConnectThreadToNull() {
        ct = null;
    }

    public static bluetoothConnectionThread.connectThread getBluetoothThread(){
        return ct;
    }

    public static bluetoothConnectionThread.AcceptThread getAcceptedThread() { return at; }


}
