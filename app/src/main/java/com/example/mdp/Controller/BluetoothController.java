package com.example.mdp.Controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothController {
    private static Activity activity;
    private static BluetoothAdapter BA;
    private static Set<BluetoothDevice> pairedDevices;
    private static DeviceListAdapter adapter;
    private static bluetoothConnectionThread.connectThread ct;
    private static String connectedDevice = "";
    private static bluetoothConnectionThread.AcceptThread at;
    private static BluetoothDevice device;
    private static boolean activeFlag = false;
    private static String msgLog = "";
    private static String mdfString = "", mdfString2 ="";
    private static String imgStrings = "";

    public static void init(Activity activity1, BluetoothAdapter bAdapter, DeviceListAdapter dlAdapter) {
        activity= activity1;
        BA = bAdapter;
        adapter = dlAdapter;
        pairedDevices = BA.getBondedDevices();
        if (!activeFlag) {
            startBluetoothServer();
        }

    }

    public static DeviceListAdapter getAdapter() { return adapter; }

    public static String getBluetoothStatus(){
        return("Bluetooth: Turned " + getBluetoothState() +"\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + getAddress(BA.getName()) + "\nConnected to: " + connectedDevice);

    }

    public static void startBluetoothServer(){
        if (at != null) {
            at.cancel();
            at = null;
        }
        at = new bluetoothConnectionThread().new AcceptThread(activity,BA);
        at.start();
        activeFlag = true;
    }

    public static void setActiveFlag(boolean flag) {
        activeFlag = flag;
    }

    public static String onBluetooth(View v){
        if (!BA.isEnabled()) {
            Toast.makeText(v.getContext(), "Turned on",Toast.LENGTH_SHORT).show();
            Boolean result = BA.enable();
            String status = (result) ? "On" : "Off";
           return("Bluetooth: Turned " + status +"\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + getAddress(BA.getName()) + "\nConnected to: " + connectedDevice);
        } else {
            Toast.makeText(v.getContext(), "Bluetooth already turned on", Toast.LENGTH_SHORT).show();
        }
        return("Bluetooth: Turned " + getBluetoothState() +"\nBluetooth Device Name: " + BA.getName() + "\nBluetooth Address: " + getAddress(BA.getName()) + "\nConnected to: " + connectedDevice);
    }

    public static String offBluetooth(View v){
        Toast.makeText(v.getContext(), "Turned off" ,Toast.LENGTH_SHORT).show();
        boolean result = BA.disable();
        String status = (result) ? "Off" : "On";
        if (ct != null)
            ct.cancel();
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
        if (name.equalsIgnoreCase("galaxy a70"))
            return "A8:34:6A:DD:BD:6D";
        else if (name.equalsIgnoreCase("MDP Grp 7"))
            return "CC:46:4E:E1:D1:D1";
        else
            return BA.getAddress();
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

    public static String getConnectedDevice() {
        return connectedDevice;
    }

    public static void attemptConnection(BluetoothDevice deviceToConnectTo, boolean autoReconnectFlag){
        if (ct != null) {
            //Log.d("LIMPEH VALUE",String.valueOf(bluetoothThread.isAlive()));
            ct.cancel();
            //bluetoothThread.setFinishedFlag(false);
            ct = null;
        }
        device = deviceToConnectTo;
        ct = new bluetoothConnectionThread().new connectThread(activity,device,BA,autoReconnectFlag);
        ct.start();
    }

    public static BluetoothDevice getConnectedBluetoothDevice() { return device; }

    public static void setConnectThreadToNull() {
        ct = null;
    }

    public static bluetoothConnectionThread.connectThread getBluetoothThread(){
        return ct;
    }

    public static bluetoothConnectionThread.AcceptThread getAcceptedThread() { return at; }

    public static void saveMsgLog(String log) {
        msgLog = log;

    }

    public static String getMsgLog() { return msgLog; }

    public static void sendCmd(String cmdString){
        if (BluetoothController.getBluetoothThread() != null)
            BluetoothController.getBluetoothThread().write(cmdString.getBytes(Charset.defaultCharset()));
        else {
            try {
                BluetoothController.getAcceptedThread().write(cmdString.getBytes(Charset.defaultCharset()));
            } catch (Exception e) {
                Log.e("BluetoothController", "Crashed here", e);
            }
        }
    }

    public static void setMdfString(String string1) { mdfString = string1; }

    public static String getMdfString() { return mdfString; }

    public static void setMdfString2(String string2) { mdfString2 = string2; }

    public static String getMdfString2() { return mdfString2; }

    public static void addImgString(String imgStr) {
        imgStrings = imgStrings + "\n" + imgStr;
    }

    public static String getImgStrings() { return imgStrings; }
}
